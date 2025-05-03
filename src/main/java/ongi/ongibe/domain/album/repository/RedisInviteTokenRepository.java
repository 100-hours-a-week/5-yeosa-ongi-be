package ongi.ongibe.domain.album.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisInviteTokenRepository implements InviteTokenRepository {

    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "INVITE:";

    private String key(String token) {
        return PREFIX + token;
    }

    @Override
    public void save(String token, Long albumId) {
        redisTemplate.opsForValue().set(key(token), albumId.toString());
    }

    @Override
    public Long getAlbumId(String token) {
        String value = redisTemplate.opsForValue().get(key(token));
        return value != null ? Long.valueOf(value) : null;
    }

    @Override
    public void remove(String token) {
        redisTemplate.delete(key(token));
    }

    @Override
    public boolean existsByToken(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(token)));
    }
}