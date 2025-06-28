if redis.call("EXISTS", KEYS[2]) == 1 then
    return 0  -- 이미 좋아요한 경우
else
    redis.call("INCR", KEYS[1])
    redis.call("SET", KEYS[2], 1)
    redis.call("EXPIRE", KEYS[2], ARGV[1])
    return 1
end