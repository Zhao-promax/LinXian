# 邻鲜履约平台（Linxian）

社区团购「次日达」履约系统。以 5 万单/天的社区团购场景为背景，从零搭建一套**可本地一键启动、可压测演示**的微服务系统，覆盖六大核心能力：

| # | 核心能力 | 关键技术 | 指标 |
|---|---------|---------|------|
| 1 | 库存防超卖 | Redis + Lua 原子预扣 + 数据库唯一索引兜底 | 下单成功率 99.7% |
| 2 | 订单全链路 | 手写状态机 + RabbitMQ 异步 + MySQL 事务 + 定时补偿 | 最终一致 |
| 3 | 并发分拣 | ThreadPoolExecutor + CountDownLatch | 3.2s → 0.8s |
| 4 | 超时自动取消 | RabbitMQ 延迟队列（TTL + 死信队列） | 免轮询 |
| 5 | 异常多维检索 | Elasticsearch bool + range 组合查询 | 秒级 → 毫秒级 |
| 6 | 容器化 + 压测 | Docker / Docker Compose + JMeter | 库存扣减 RT 15ms |

## 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 17 | — |
| Spring Boot | 4.1.1 | — |
| Spring Cloud | 2025.1.3 | Gateway / OpenFeign / LoadBalancer |
| Spring Cloud Alibaba | 2025.1.0.0 | Nacos 3.1.1 注册发现 |
| MyBatis-Plus | 3.5.17 | `mybatis-plus-spring-boot4-starter` |
| MySQL | 8.0 | 每服务独立 schema |
| Redis | 7.x | Lettuce + `DefaultRedisScript` 执行 Lua |
| RabbitMQ | 3.13 | 延迟队列 + 异步通知 |
| Elasticsearch | 8.19.23 | 官方 `elasticsearch-java` 客户端 |
| JWT | jjwt 0.13.0 | 用户服务签发，网关统一校验 |
| JMeter | 5.6+ | 压测脚本（`jmeter/`） |

## 架构

```
                          ┌────────────────────────────┐
   消费者 / 团长 ─────────▶│  Gateway :8080 (JWT 鉴权+路由) │
                          └──────────────┬─────────────┘
                                         │ Nacos 注册发现
      ┌──────────┬──────────┬───────────┼───────────┬─────────────┐
      ▼          ▼          ▼           ▼           ▼             ▼
  user :8081  leader:8082  order:8084  inventory:8083  fulfillment:8085
   注册/登录   团长/自提点   下单/状态机    Redis+Lua预扣    线程池分拣
   JWT 签发               延迟队列       回补/确认        异常补偿+ES检索
      └──────────┴──────────┴─────▲─────┴───────────┴─────▲───────┘
                                  │      RabbitMQ（异步通知 / 延迟队列）
                                  └────────────────────────┘
```

**同步调用（OpenFeign）**：order → inventory（预扣/回补/确认）、order → leader（校验自提点）、fulfillment → order（回调状态/查明细）。
**异步调用（RabbitMQ）**：order → fulfillment（支付成功触发分拣）、order 延迟队列（超时取消）。

## 模块结构

```
linxian/
├── linxian-common/            # Result 统一返回、异常、常量、状态机枚举、实体基类
├── linxian-api/               # Feign 接口 + 共享 DTO
├── linxian-gateway/           # 网关（JWT 全局过滤器 + 路由）
├── linxian-user-service/      # 用户（BCrypt 注册、JWT 登录）
├── linxian-leader-service/    # 团长 / 自提点
├── linxian-inventory-service/ # 库存（Redis+Lua 预扣）
├── linxian-order-service/     # 订单（状态机 + 延迟队列）
├── linxian-fulfillment-service/ # 履约（并发分拣 + 异常补偿 + ES）
├── sql/                       # 5 个 schema 建库建表 + 演示数据
├── docker-compose.yml         # 中间件编排（MySQL/Redis/RabbitMQ/ES/Nacos）
├── docker-compose.app.yml     # 应用服务容器化编排
├── Dockerfile                 # 通用多阶段构建（ARG SERVICE_MODULE）
├── jmeter/                    # 压测脚本
└── docs/                      # 架构、演示手册、面试问答、压测报告
```

## 快速启动

### 1. 启动中间件

```bash
docker compose up -d
```

首次启动 MySQL 会自动执行 `sql/` 下的建库建表与演示数据脚本。默认账号：MySQL `root/linxian123`，RabbitMQ `guest/guest`，Redis/Nacos/ES 无密码。

### 2. 启动服务（二选一）

**IDE 方式**：分别运行 6 个模块的 `*Application` 启动类，顺序任意（Nacos 会自动重连）。

**命令行方式**（JDK 17）：

```bash
mvn -pl linxian-user-service -am spring-boot:run
mvn -pl linxian-leader-service -am spring-boot:run
mvn -pl linxian-inventory-service -am spring-boot:run
mvn -pl linxian-order-service -am spring-boot:run
mvn -pl linxian-fulfillment-service -am spring-boot:run
mvn -pl linxian-gateway -am spring-boot:run
```

### 3. 全部容器化启动

```bash
docker compose -f docker-compose.yml -f docker-compose.app.yml up -d --build
```

## 全链路演示（curl）

> 完整可复制的脚本见 [docs/demo-guide.md](docs/demo-guide.md)，这里给出关键步骤。

```bash
# 1. 注册 + 登录，拿到 token
curl -s -X POST http://localhost:8080/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo123456"}'

TOKEN=$(curl -s -X POST http://localhost:8080/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo123456"}' | python -c "import sys,json;print(json.load(sys.stdin)['data']['token'])")

# 2. 下单（pickupPointId=1 幸福小区自提点，skuId=1 苹果）
ORDER_NO=$(curl -s -X POST http://localhost:8080/order/create \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"pickupPointId":1,"items":[{"skuId":1,"productName":"新疆阿克苏苹果","skuSpec":"5斤/箱","quantity":2,"price":29.90}]}' \
  | python -c "import sys,json;print(json.load(sys.stdin)['data']['orderNo'])")

# 3. 支付（mock），触发履约分拣，订单 PAID → SORTING → READY_FOR_PICKUP
curl -s -X POST http://localhost:8080/order/pay/$ORDER_NO \
  -H "Authorization: Bearer $TOKEN"

# 4. 查询订单状态
curl -s http://localhost:8080/order/$ORDER_NO -H "Authorization: Bearer $TOKEN"

# 5. 自提完成
curl -s -X POST http://localhost:8080/order/pickup/$ORDER_NO \
  -H "Authorization: Bearer $TOKEN"
```

**超时取消演示**：把 `linxian-order-service` 的 `order.timeout-millis` 临时改为 `30000`，下单后不支付，30 秒后订单自动 CANCELLED、库存自动回补。

**并发分拣基准**（串行 vs 并发）：

```bash
curl -s -X POST http://localhost:8080/fulfillment/benchmark
# => {"itemCount":4,"perItemMillis":800,"serialMs":3201,"concurrentMs":802,"speedup":"3.99x"}
```

**履约异常检索**（先造一条异常，再按团长/商品/时间查）：

```bash
curl -s -X POST "http://localhost:8080/fulfillment/exception/simulate?leaderId=1&skuId=1"
curl -s "http://localhost:8080/fulfillment/exception/search?leaderId=1&skuId=1"
```

## 六大核心功能实现要点

1. **Redis + Lua 防超卖**：Lua 脚本内「读可用库存 → 校验 → DECRBY」原子完成（`linxian-inventory-service/src/main/resources/lua/pre_deduct.lua`）；`stock_deduct_log` 唯一索引 `(order_id, sku_id, type)` 兜底幂等；DB 侧条件更新 `available >= qty` 双保险。
2. **订单状态机**：`OrderStatus × OrderEvent → 目标状态` 转移表（手写 enum，见 `OrderStateMachine`），状态更新 SQL 带 `WHERE status = 原状态` 防止并发重复流转；支付/取消经 RabbitMQ 异步通知，失败由延迟队列 + 定时任务补偿。
3. **并发分拣**：自定义 `ThreadPoolExecutor`（有界队列 + 命名线程工厂 + CallerRunsPolicy），`CountDownLatch` 等待子任务汇总；`/fulfillment/benchmark` 对比串行/并发耗时。
4. **延迟队列超时取消**：`order.delay.queue` 设 TTL，到期投递死信 `order.cancel.queue`，消费者校验仍为待支付才取消并回补库存。
5. **ES 多维检索**：履约异常先落库再同步 ES（失败仅告警不影响主流程），`bool` + `term` + `range` 组合查询，ES 不可用时自动降级 DB。
6. **容器化 + 压测**：中间件与应用服务均可一键编排；`jmeter/` 提供库存预扣、下单链路脚本。

## 压测

```bash
# 压测前调大库存，避免打空
docker exec -i linxian-mysql mysql -uroot -plinxian123 \
  -e "UPDATE linxian_inventory.stock SET available=1000000 WHERE sku_id=1;"

# 库存预扣（直连 8083，绕网关）
jmeter -n -t jmeter/inventory-pre-deduct.jmx -l result.jtl -e -o report

# 下单链路（经网关 8080，含 JWT）
jmeter -n -t jmeter/order-create.jmx -l result.jtl -e -o report
```

目标指标：库存扣减平均 RT ≈ 15ms、成功率 ≥ 99.7%（详见 [docs/stress-test-report.md](docs/stress-test-report.md)）。

## 文档

- [架构与设计说明](docs/architecture.md)
- [演示手册](docs/demo-guide.md)
- [面试问答要点](docs/interview-qa.md)
- [压测报告](docs/stress-test-report.md)
