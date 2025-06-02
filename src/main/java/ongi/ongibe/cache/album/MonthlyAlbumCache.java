package ongi.ongibe.cache.album;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MonthlyAlbumCache {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration TTL = Duration.ofHours(7);

    public MonthlyAlbumResponseDTO get(Long userId, String yearMonth) {
        String key = makeKey(userId, yearMonth);
        return (MonthlyAlbumResponseDTO) redisTemplate.opsForValue().get(key);
    }

    public void set(Long userId, String yearMonth, MonthlyAlbumResponseDTO value) {
        String key = makeKey(userId, yearMonth);
        redisTemplate.opsForValue().set(key, value, TTL);
    }

    public void delete(Long userId, String yearMonth) {
        String key = makeKey(userId, yearMonth);
        redisTemplate.delete(key);
    }

    private String makeKey(Long userId, String yearMonth) {
        return "userId:" + userId + ":monthlyAlbum:" + yearMonth;
    }

}
