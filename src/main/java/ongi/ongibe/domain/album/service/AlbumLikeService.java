package ongi.ongibe.domain.album.service;

import io.micrometer.core.aop.CountedAspect;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.AlbumLikeResponseDTO;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.AlbumLike;
import ongi.ongibe.domain.album.exception.AlbumException;
import ongi.ongibe.domain.album.repository.AlbumLikeRepository;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.cache.CacheKeyUtil;
import ongi.ongibe.global.cache.RedisCacheService;
import ongi.ongibe.global.security.util.SecurityUtil;
import org.springdoc.core.service.SecurityService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlbumLikeService {

    private final AlbumLikeRepository albumLikeRepository;
    private final AlbumRepository albumRepository;
    private final SecurityUtil securityUtil;
    private final RedisCacheService redisCacheService;
    private final DefaultRedisScript<Long> likeScript;
    private final DefaultRedisScript<Long> dislikeScript;
    private final CountedAspect countedAspect;

    public AlbumLikeService(
            AlbumLikeRepository albumLikeRepository,
            AlbumRepository albumRepository,
            SecurityUtil securityUtil,
            RedisCacheService redisCacheService,
            @Qualifier("likeScript") DefaultRedisScript<Long> likeScript,
            @Qualifier("dislikeScript") DefaultRedisScript<Long> dislikeScript,
            CountedAspect countedAspect) {
        this.albumLikeRepository = albumLikeRepository;
        this.albumRepository = albumRepository;
        this.securityUtil = securityUtil;
        this.redisCacheService = redisCacheService;
        this.likeScript = likeScript;
        this.dislikeScript = dislikeScript;
        this.countedAspect = countedAspect;
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
        return like == 1;
    }

    private void dislikeAlbum(Long albumId, Long userId){
        String countKey = CacheKeyUtil.albumLikeCountKey(albumId);
        String userKey =  CacheKeyUtil.albumUserLikedKey(albumId, userId);

        redisCacheService.execute(dislikeScript, List.of(countKey, userKey), TTL);
    }

    private int getLiked(Long albumId) {
        String countKey = CacheKeyUtil.albumLikeCountKey(albumId);
        return redisCacheService.get(countKey, Integer.class).orElse(0);
    }
}
