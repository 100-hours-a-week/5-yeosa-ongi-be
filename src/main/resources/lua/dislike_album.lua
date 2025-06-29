if redis.call("GET", KEYS[2]) == "0" then
    return 0  -- 좋아요 안 한 경우
else
    redis.call("DECR", KEYS[1])
    redis.call("SET", KEYS[2], "0")
    return 1
end