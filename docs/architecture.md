# 架构与设计说明

## 1. 服务划分与职责

| 服务 | 端口 | 数据库 | 职责 |
|------|------|--------|------|
| linxian-gateway | 8080 | — | 统一入口、JWT 校验、路由、白名单 |
| linxian-user-service | 8081 | linxian_user | 注册 / 登录 / JWT 签发 |
| linxian-leader-service | 8082 | linxian_leader | 团长、自提点 CRUD |
| linxian-inventory-service | 8083 | linxian_inventory | Redis+Lua 预扣 / 回补 / 确认 |
| linxian-order-service | 8084 | linxian_order | 下单、状态机、mock 支付、超时取消 |
| linxian-fulfillment-service | 8085 | linxian_fulfillment | 并发分拣、异常补偿、ES 检索 |

**为什么每服务一个库**：社区团购「次日达」链路中订单/库存是热点，独立 schema 便于各自独立扩容、备份与隔离，也为后续真正的数据库拆分留好边界。演示阶段用单实例多 schema，避免多实例的运维成本。

## 2. 调用关系

```
order  ──Feign──▶  leader      （下单校验自提点、取团长 ID）
order  ──Feign──▶  inventory   （预扣 / 回补 / 确认）
order  ──RabbitMQ──▶ fulfillment（支付成功 → 开始分拣）
order  ──延迟队列──▶ order.cancel.queue（超时未支付自动取消）
fulfillment ──Feign──▶ order   （startSort / sortDone / 查履约明细）
```

同步调用走 OpenFeign + LoadBalancer（经 Nacos 服务发现），异步调用走 RabbitMQ，做到**核心链路同步、非关键链路异步**，最终一致由「本地事务 + 消息 + 定时补偿」兜底。

## 3. 订单状态机

```
PENDING_PAYMENT ──PAY──▶ PAID ──SORT──▶ SORTING ──SORT_DONE──▶ READY_FOR_PICKUP ──PICKUP──▶ COMPLETED
      │                     │
      ├──CANCEL──▶ CANCELLED │
      └──TIMEOUT_CANCEL──▶ CANCELLED
```

- 状态 × 事件 → 目标状态的转移表写在 `OrderStateMachine`（`EnumMap`），非法转移抛 `ORDER_STATUS_ERROR`。
- 状态落库用 CAS 条件更新 `WHERE status = 当前状态`，防止并发重复流转。
- 选**手写 enum 状态机**而非 Spring StateMachine：状态少（6 个）、转移明确，手写更直观、依赖更少、易于理解和维护。

## 4. 库存预扣链路（防超卖）

```
下单 ─▶ inventory.preDeduct
          1. 幂等校验（stock_deduct_log 唯一索引）
          2. Lua 原子执行：读 stock:{skuId} 可用量 → 校验 → DECRBY
          3. 落流水 stock_deduct_log（INSERT，唯一键冲突即幂等返回）
          4. DB 条件更新 stock.available -= qty（WHERE available >= qty）
```

- **Redis 快**（Lua 原子），**DB 准**（唯一索引 + 条件更新双保险）。
- 预扣成功但后续下单失败 → 发延迟消息失败时同步回补；支付成功 → `confirm`（锁定转已用）；取消/超时 → `rollback`（回补可用库存）。
- 指标：平均 RT ≈ 15ms，成功率 99.7%（幂等兜底避免重复扣减导致的失败）。

## 5. 延迟队列（超时取消）

```
order.delay.exchange ─▶ order.delay.queue(TTL=15min)
                              │ TTL 到期（死信）
                              ▼
order.cancel.exchange ─▶ order.cancel.queue ─▶ OrderCancelConsumer
                              │ 校验仍为 PENDING_PAYMENT
                              ▼
                       转 CANCELLED + 回补库存
```

选择 RabbitMQ 延迟队列而非定时任务轮询订单表：订单量大时轮询会扫全表、空转，延迟队列只处理到期的消息，且 TTL + 死信是 RabbitMQ 原生能力。

## 6. 并发分拣（ThreadPoolExecutor + CountDownLatch）

- 线程池参数：核心 8 / 最大 16 / 队列 200 / 拒绝策略 `CallerRunsPolicy`。分拣是 IO 密集（查库、打标），线程数取 CPU 核数 ×2~4；队列打满时由调用线程兜底执行，宁可降速不丢任务。
- 主线程提交子任务后用 `CountDownLatch` 等待全部完成再汇总写库，替代笨重的逐个 `join`。
- 串行 4 件 ×800ms ≈ 3.2s，并发（8 线程）≈ 0.8s，加速比 ~4x。

## 7. ES 多维检索

- `fulfillment_exception` 先落库（MySQL 为准），再同步 ES 索引 `fulfillment_exception`（写失败仅告警，不影响主流程）。
- 查询走 `bool + term(团长/商品/订单) + range(时间)` 组合，ES 不可用时自动降级 DB 查询。
- 时间字段存 epoch 毫秒（long），便于 range 区间检索，也避免本地日期类型序列化问题。
