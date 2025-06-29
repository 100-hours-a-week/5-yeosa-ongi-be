package ongi.ongibe.domain.album.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.AlbumLikeResponseDTO;
import ongi.ongibe.domain.album.repository.AlbumLikeRepository;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.global.cache.CacheKeyUtil;
import ongi.ongibe.global.cache.RedisCacheService;
import ongi.ongibe.global.security.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AlbumLikeService {

    private final AlbumLikeRepository albumLikeRepository;
    private final SecurityUtil securityUtil;
    private final RedisCacheService redisCacheService;
    private final DefaultRedisScript<Long> likeScript;
    private final DefaultRedisScript<Long> dislikeScript;

    public AlbumLikeService(
            AlbumLikeRepository albumLikeRepository,
            SecurityUtil securityUtil,
            RedisCacheService redisCacheService,
            @Qualifier("likeScript") DefaultRedisScript<Long> likeScript,
            @Qualifier("dislikeScript") DefaultRedisScript<Long> dislikeScript) {
        this.albumLikeRepository = albumLikeRepository;
        this.securityUtil = securityUtil;
        this.redisCacheService = redisCacheService;
        this.likeScript = likeScript;
        this.dislikeScript = dislikeScript;
    }

    private static final long TTL = 60L * 60L * 24L * 30L;

    @Transactional
    public BaseApiResponse<AlbumLikeResponseDTO> albumLikeToggle(Long albumId){
        User user = securityUtil.getCurrentUser();
        Long userId = user.getId();
        boolean isLiked = toggleLike(albumId, userId);
        AlbumLikeResponseDTO dto;
        if (isLiked){
            int likeCount = getLiked(albumId);
            dto = new AlbumLikeResponseDTO(true, likeCount);
        } else {
            dislikeAlbum(albumId, userId);
            int likeCount = getLiked(albumId);
            dto = new AlbumLikeResponseDTO(false, likeCount);
        }
        return BaseApiResponse.success(
                "LIKE_SUCCESS",
                "좋아요 연산 성공했습니다.",
                dto
        );
    }

    private boolean toggleLike(Long albumId, Long userId){
        String countKey = CacheKeyUtil.albumLikeCountKey(albumId);
        String userKey =  CacheKeyUtil.albumUserLikedKey(albumId, userId);

        Long like = redisCacheService.execute(likeScript, List.of(countKey, userKey), TTL);
        return like != null && like == 1L;
    }

    private void dislikeAlbum(Long albumId, Long userId){
        String countKey = CacheKeyUtil.albumLikeCountKey(albumId);
        String userKey =  CacheKeyUtil.albumUserLikedKey(albumId, userId);

        try {
            redisCacheService.execute(dislikeScript, List.of(countKey, userKey), TTL);
        } catch (Exception e) {
            log.error("DISLIKE Lua Error: {}", e.getMessage(), e);
        }
    }

    private int getLiked(Long albumId) {
        String countKey = CacheKeyUtil.albumLikeCountKey(albumId);
        Optional<Integer> cachedCount = redisCacheService.get(countKey, Integer.class);
        if (cachedCount.isPresent()) {return cachedCount.get();}

        int likeCount = albumLikeRepository.countByAlbumId(albumId);
        redisCacheService.set(countKey, likeCount, Duration.ofDays(30));
        return likeCount;
    }
}
