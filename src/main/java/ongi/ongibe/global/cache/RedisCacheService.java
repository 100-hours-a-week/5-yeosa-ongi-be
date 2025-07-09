package ongi.ongibe.global.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> objectRedisTemplate;
    private final ObjectMapper objectMapper;

    public <T> void set(String key, T value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (JsonProcessingException e) {
            log.error("RedisCache 저장 실패 (key: {}): {}", key, e.getMessage(), e);
        }
    }

    public <T> Optional<T> get(String key, Class<T> clazz) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null)
                return Optional.empty();
            return Optional.of(objectMapper.readValue(json, clazz));
        } catch (Exception e) {
            log.error("RedisCache 조회 실패 (key: {}): {}", key, e.getMessage(), e);
            return Optional.empty();
        }
    }

    public void evict(String key) {
        redisTemplate.delete(key);
    }

    public <T> T execute(
            RedisScript<T> script,
            List<String> keys,
            Object... args
    ) {
        try {
            return objectRedisTemplate.execute(script, keys, args);
        } catch (Exception e) {
            log.error("Redis Lua Script 실행 실패 (keys: {}, args: {}): {}", keys, args, e.getMessage(), e);
            return null;
        }
    }
}