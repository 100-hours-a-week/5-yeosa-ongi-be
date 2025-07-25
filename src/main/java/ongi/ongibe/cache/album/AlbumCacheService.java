package ongi.ongibe.cache.album;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO.AlbumInfo;
import ongi.ongibe.domain.album.entity.UserAlbum;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.cache.CacheKeyUtil;
import ongi.ongibe.global.cache.RedisCacheService;
import ongi.ongibe.global.security.util.SecurityUtil;
import ongi.ongibe.global.util.DateUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumCacheService {

    private final UserAlbumRepository userAlbumRepository;
    private final SecurityUtil securityUtil;
    private final RedisCacheService redisCacheService;
    private final UserRepository userRepository;

    private static final Duration TTL = Duration.ofSeconds(10);
    private static final Duration LockTTL = Duration.ofSeconds(3);

    public MonthlyAlbumResponseDTO getMonthlyAlbum(Long userId, String requestYearMonth) {
        String yearMonth = String.valueOf(DateUtil.parseOrNow(requestYearMonth));
        String key = CacheKeyUtil.key("monthlyAlbum", userId, yearMonth);
        String lockKey = key + ":lock";

        Optional<MonthlyAlbumResponseDTO> cached = redisCacheService.get(key, MonthlyAlbumResponseDTO.class);
        if (cached.isPresent()) {
            return cached.get();
        }
        boolean getLock = redisCacheService.tryLock(lockKey, LockTTL);
        if (getLock) {
            try{
                User user = securityUtil.getCurrentUser();
                MonthlyAlbumResponseDTO response = buildMonthlyAlbumResponse(user, yearMonth);
                redisCacheService.set(key, response, TTL);
                return response;
            } finally {
                redisCacheService.unlock(lockKey);
            }
        }
        int retry = 5;
        while (retry-- > 0) {
            try{
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Optional<MonthlyAlbumResponseDTO> retryCache = redisCacheService.get(key, MonthlyAlbumResponseDTO.class);
            if (retryCache.isPresent()) {
                return retryCache.get();
            }
        }
        throw new IllegalStateException("캐시 로딩 실패 : " + key);
    }

    private List<AlbumInfo> getAlbumInfos(List<UserAlbum> userAlbumList, String yearMonth) {
        LocalDateTime start = DateUtil.getStartOfMonth(yearMonth);
        LocalDateTime end = DateUtil.getEndOfMonth(yearMonth);

        return userAlbumList.stream()
                .map(UserAlbum::getAlbum)
                .filter(album -> !album.getCreatedAt().isBefore(start) && !album.getCreatedAt().isAfter(end))
                .map(AlbumInfo::of)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void evictMonthlyAlbum(Long userId, String yearMonth) {
        String key = CacheKeyUtil.key("monthlyAlbum", userId, yearMonth);
        redisCacheService.evict(key);
    }

    public void refreshMonthlyAlbum(Long userId, String yearMonth) {
        String key = CacheKeyUtil.key("monthlyAlbum", userId, yearMonth);
        User user = userRepository.findById(userId).orElseThrow();
        MonthlyAlbumResponseDTO response = buildMonthlyAlbumResponse(user, yearMonth);
        redisCacheService.set(key, response, TTL);

    }

    public void refreshAllMonthlyAlbumCache(Long userId) {
        List<String> yearMonths = userAlbumRepository.findAllYearMonthsByUserId(userId);
        for (String yearMonth : yearMonths) {
            refreshMonthlyAlbum(userId, yearMonth);
        }
    }

    private MonthlyAlbumResponseDTO buildMonthlyAlbumResponse(User user, String yearMonth) {
        List<UserAlbum> userAlbumList = userAlbumRepository.findAllByUser(user);
        List<AlbumInfo> albumInfos = getAlbumInfos(userAlbumList, yearMonth);

        boolean hasNext = userAlbumRepository.existsByUserAndAlbum_CreatedAtBefore(user, DateUtil.getStartOfMonth(yearMonth));
        String nextYearMonth = hasNext ? DateUtil.getPreviousYearMonth(yearMonth) : null;

        return new MonthlyAlbumResponseDTO(albumInfos, nextYearMonth, hasNext);
    }

}
