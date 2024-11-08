local key = KEYS[1]

local window_size = tonumber(ARGV[1])
local limit = tonumber(ARGV[2])
local timestamp = tonumber(ARGV[3])
local member = ARGV[4]

local accepted = 0
local exists_key = redis.call('exists', key)
if (exists_key == 1) then
    accepted = redis.call('zcard', key)
end

if (accepted < limit) then
    redis.call('zadd', key, timestamp, member)
end

redis.call('zremrangebyscore', key, 0, timestamp - window_size)
redis.call('expire', key, window_size)

local remain = limit - accepted

local ttl = redis.call('ttl', key)
return { key, ttl, remain }