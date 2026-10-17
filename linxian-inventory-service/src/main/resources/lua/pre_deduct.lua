-- 库存原子预扣脚本（多 SKU 全量成功或全量失败）
-- KEYS: stock:{skuId} 列表，与 ARGV 一一对应
-- ARGV: 每项扣减数量
-- 返回: 1 成功, 0 库存不足, -1 库存未初始化
local n = #KEYS

-- 先整体校验，避免部分扣减
for i = 1, n do
    local current = tonumber(redis.call('GET', KEYS[i]) or '-1')
    if current < 0 then
        return -1
    end
    if current < tonumber(ARGV[i]) then
        return 0
    end
end

-- 全部充足，统一扣减
for i = 1, n do
    redis.call('DECRBY', KEYS[i], tonumber(ARGV[i]))
end

return 1
