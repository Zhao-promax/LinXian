# 演示手册

> 前置：`docker compose up -d` 已启动中间件，6 个服务已启动（默认端口 8080~8085）。
> 已内置演示数据：SKU 1/2（各 1000 库存），团长 1/2，自提点 1/2。

## 1. 注册与登录

```bash
curl -s -X POST http://localhost:8080/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo123456","nickname":"演示用户"}'
# 首次返回 code=0；重复注册返回 code=1001（用户已存在）

curl -s -X POST http://localhost:8080/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo123456"}'
# data.token 即 JWT
```

提取 token（Windows Git Bash 可用 python）：

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo123456"}' \
  | python -c "import sys,json;print(json.load(sys.stdin)['data']['token'])")
```

## 2. 下单 → 支付 → 分拣 → 自提（全链路）

```bash
# 下单：自提点 1（幸福小区），SKU 1（苹果 5 斤）2 件
RESP=$(curl -s -X POST http://localhost:8080/order/create \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"pickupPointId":1,"items":[{"skuId":1,"productName":"新疆阿克苏苹果","skuSpec":"5斤/箱","quantity":2,"price":29.90}]}')
echo $RESP
ORDER_NO=$(echo $RESP | python -c "import sys,json;print(json.load(sys.stdin)['data']['orderNo'])")

# 查询：status=0（待支付）
curl -s http://localhost:8080/order/$ORDER_NO -H "Authorization: Bearer $TOKEN"

# 支付（mock）：订单 PAID，并异步触发履约分拣
curl -s -X POST http://localhost:8080/order/pay/$ORDER_NO -H "Authorization: Bearer $TOKEN"

# 稍候 1~2 秒，再查：status 应已走到 3（待自提，READY_FOR_PICKUP）
sleep 2
curl -s http://localhost:8080/order/$ORDER_NO -H "Authorization: Bearer $TOKEN"

# 自提：status=4（已完成）
curl -s -X POST http://localhost:8080/order/pickup/$ORDER_NO -H "Authorization: Bearer $TOKEN"
```

状态码：`0 待支付 / 1 已支付 / 2 分拣中 / 3 待自提 / 4 已完成 / 5 已取消`。

## 3. 超时自动取消

1. 把 `linxian-order-service/src/main/resources/application.yml` 的 `order.timeout-millis` 改为 `30000`（30 秒）后重启订单服务。
2. 下单后不支付，30 秒后订单自动 `CANCELLED`，库存自动回补。

```bash
ORDER_NO=$(curl -s -X POST http://localhost:8080/order/create \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"pickupPointId":1,"items":[{"skuId":2,"productName":"山东红富士苹果","skuSpec":"10斤/箱","quantity":1,"price":49.90}]}' \
  | python -c "import sys,json;print(json.load(sys.stdin)['data']['orderNo'])")
sleep 35
curl -s http://localhost:8080/order/$ORDER_NO -H "Authorization: Bearer $TOKEN"
# status=5（已取消）
```

## 4. 并发分拣基准

```bash
curl -s -X POST http://localhost:8080/fulfillment/benchmark
# {"itemCount":4,"perItemMillis":800,"serialMs":3201,"concurrentMs":802,"speedup":"3.99x"}
```

可传参对比：`?itemCount=8&sleepMillis=500`。

## 5. 履约异常 + ES 检索 + 定时补偿

```bash
# 造一条异常（团长 1、SKU 1）
curl -s -X POST "http://localhost:8080/fulfillment/exception/simulate?leaderId=1&skuId=1"

# 多维检索：按团长 / 商品 / 订单号 / 时间区间组合
curl -s "http://localhost:8080/fulfillment/exception/search?leaderId=1&skuId=1"
curl -s "http://localhost:8080/fulfillment/exception/search?leaderId=1"
curl -s "http://localhost:8080/fulfillment/exception/search?from=0&to=9999999999999"

# 30 秒后（补偿间隔），该异常 status 由 0（待处理）→ 1（已补偿）
# 可在 MySQL 查：SELECT id,status,retry_count FROM linxian_fulfillment.fulfillment_exception;
```

真实分拣失败演示：把 `sort.fail-rate` 调为 `0.5` 后重启履约服务，再走一次「支付」，分拣会按概率失败并自动记录异常、进入补偿。

## 6. 压测

```bash
docker exec -i linxian-mysql mysql -uroot -plinxian123 \
  -e "UPDATE linxian_inventory.stock SET available=1000000 WHERE sku_id=1;"
jmeter -n -t jmeter/inventory-pre-deduct.jmx -l result.jtl -e -o report
```
