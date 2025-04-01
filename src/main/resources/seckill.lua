-- 所需參數列表
-- 1.優惠券ID
local voucherId = ARGV[1]
-- 2.用戶ID
local userId = ARGV[2]
-- 3.訂單ID
local orderId = ARGV[3]

-- 數據key
-- 1.庫存key
local stockKey = 'seckill:stock:' .. voucherId;
-- 2.訂單ley
local orderKey = 'order:stock:' .. voucherId;

-- 業務流程
-- 1.判斷庫存是否充足 get stockKey
if (tonumber(redis.call('get', stockKey)) <= 0) then
    -- 庫存不足，返回 -1
    return -1;
end
-- 2.判斷用戶是否下單 SISMEMBER orderKey userId
if (redis.call('sismember', orderKey, userId) == 1) then
    -- 存在，重複下單，返回 2
    return 2;
end
-- 3.扣庫存 incrby stockKey - 1
 redis.call('incrBy', stockKey, -1)
-- 4.下單(保存用戶) sadd orderKey userId
redis.call('sadd', orderKey, userId)
-- 5.發送消息到隊列，XADD stream.orders * k1 v1 k2 v2 ...
redis.call('xadd', 'stream.orders', '*', 'userId', userId, 'voucherId', voucherId, 'id', orderId)
return 0;
