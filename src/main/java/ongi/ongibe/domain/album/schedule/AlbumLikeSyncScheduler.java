package ongi.ongibe.domain.album.schedule;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.album.repository.AlbumLikeRepository;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.cache.RedisCacheService;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class AlbumLikeSyncScheduler {

    private final AlbumLikeRepository albumLikeRepository;
    private final AlbumRepository albumRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisCacheService redisCacheService;

    @Scheduled(cron = "0/10 * * * * *")
    @Transactional
    public void syncAlbumLikeData() {
        log.info("[SYNC] Redis → DB 좋아요 데이터 동기화 시작");

        ScanOptions options = ScanOptions.scanOptions()
                .match("cache::album:*")
                .count(1000)
                .build();

        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();

                try {
                    if (key.endsWith("like_count")) {
                        syncLikeCount(key);
                    } else if (key.contains(":liked:")) {
                        log.info("Processing liked key: {}", key);
                        syncUserLike(key);
                    }
                } catch (Exception e) {
                    log.error("[SYNC] 키 처리 실패 (key: {}): {}", key, e.getMessage(), e);
                }
            }
        }

        log.info("[SYNC] Redis → DB 좋아요 데이터 동기화 완료");
    }


    private void syncLikeCount(String key) {
        Long albumId = extractAlbumIdFromLikeCountKey(key);
        if (albumId == null) return;

        Optional<Integer> likeCount = redisCacheService.get(key, Integer.class);
        if (likeCount.isEmpty()) return;

        log.info("albumId: {}, likeCount: {}", albumId, likeCount);
        albumRepository.updateLikeCount(albumId, likeCount.get());
    }

    private void syncUserLike(String key) {
        try {
            Long albumId = extractAlbumIdFromLikedKey(key);
            Long userId = extractUserIdFromLikedKey(key);
            if (albumId == null || userId == null) {
                log.warn("잘못된 key 구조로 인해 albumId 또는 userId 추출 실패 (key: {})", key);
                return;
            }

            int liked = redisCacheService.get(key, Integer.class).orElse(0);
            log.info("albumId: {}, userId: {}", albumId, userId);
            log.info("liked: {}", liked);

            if (liked == 1) {
                albumLikeRepository.upsert(albumId, userId);
                log.info("albumLikeRepository.upsert(albumId={}, userId={}) 호출", albumId, userId);
            } else {
                albumLikeRepository.deleteByAlbumIdAndUserId(albumId, userId);
                log.info("albumLikeRepository.deleteByAlbumIdAndUserId(albumId={}, userId={}) 호출", albumId, userId);
            }
        } catch (Exception e) {
            log.error("[SYNC] syncUserLike 처리 중 예외 발생 (key: {}): {}", key, e.getMessage(), e);
        }
    }


    private Long extractAlbumIdFromLikedKey(String key) {
        try {
            String[] parts = key.split("::", 2)[1].split(":");
            return Long.parseLong(parts[1]);
        } catch (Exception e) {
            return null;
        }
    }

    private Long extractUserIdFromLikedKey(String key) {
        try {
            String[] parts = key.split("::", 2)[1].split(":");
            return Long.parseLong(parts[3]);
        } catch (Exception e) {
            return null;
        }
    }

    private Long extractAlbumIdFromLikeCountKey(String key) {
        try {
            String[] parts = key.split("::", 2)[1].split(":");
            return Long.parseLong(parts[1]);
        } catch (Exception e) {
            return null;
        }
    }
}
