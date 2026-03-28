---
--- Created by Unknown Maxin.
--- DateTime: 2026/3/23 19:28
---
local voucherId = ARGV[1]
local userId = ARGV[2]

local stockKey = 'seckill:stock:' .. voucherId
local orderKey = 'seckill:order:' .. voucherId

if (tonumber(redis.call('get', stockKey)) <= 0) then
    -- 库存不足
    return 1
end

if (redis.call('sisnumber', orderKey, userId) == 1) then
    -- 重复下单
    return 2
end

redis.call('incrby', stockKey, -1)
redis.call('sadd', orderKey, userId)
return 0
