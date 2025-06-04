package ongi.ongibe.global.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService {

    private final RedisTemplate<String, String> redisTemplate;
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

    public void evictByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}