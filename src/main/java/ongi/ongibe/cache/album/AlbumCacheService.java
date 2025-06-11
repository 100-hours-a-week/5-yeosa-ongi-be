package ongi.ongibe.cache.album;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
import ongi.ongibe.util.DateUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumCacheService {

    private final UserAlbumRepository userAlbumRepository;
    private final SecurityUtil securityUtil;
    private final RedisCacheService redisCacheService;
    private final UserRepository userRepository;

    private static final Duration TTL = Duration.ofMinutes(10);

    public MonthlyAlbumResponseDTO getMonthlyAlbum(Long userId, String requestYearMonth) {
        String yearMonth = String.valueOf(DateUtil.parseOrNow(requestYearMonth));
        String key = CacheKeyUtil.key("monthlyAlbum", userId, yearMonth);
        return redisCacheService.get(key, MonthlyAlbumResponseDTO.class).orElseGet(() ->{
            User user = securityUtil.getCurrentUser();
            MonthlyAlbumResponseDTO response = buildMonthlyAlbumResponse(user, yearMonth);
            redisCacheService.set(key, response, TTL);
            return response;
        });
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

    private MonthlyAlbumResponseDTO buildMonthlyAlbumResponse(User user, String yearMonth) {
        List<UserAlbum> userAlbumList = userAlbumRepository.findAllByUser(user);
        List<AlbumInfo> albumInfos = getAlbumInfos(userAlbumList, yearMonth);

        boolean hasNext = userAlbumRepository.existsByUserAndAlbum_CreatedAtBefore(user, DateUtil.getStartOfMonth(yearMonth));
        String nextYearMonth = hasNext ? DateUtil.getPreviousYearMonth(yearMonth) : null;

        return new MonthlyAlbumResponseDTO(albumInfos, nextYearMonth, hasNext);
    }
}
