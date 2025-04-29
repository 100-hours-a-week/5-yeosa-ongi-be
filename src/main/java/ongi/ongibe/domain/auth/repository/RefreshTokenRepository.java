package ongi.ongibe.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "refreshToken:";

    public void save(Long userId, String refreshToken, long expirationSeconds) {
        redisTemplate.opsForValue()
                .set(PREFIX + userId, refreshToken, Duration.ofSeconds(expirationSeconds));
    }

    public String findByUserId(Long userId) {
        return redisTemplate.opsForValue().get(PREFIX + userId);
    }

    public void delete(Long userId) {
        redisTemplate.delete(PREFIX + userId);
    }
}

