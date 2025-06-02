package ongi.ongibe.cache.album;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO.AlbumInfo;
import ongi.ongibe.domain.album.entity.UserAlbum;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.global.security.util.SecurityUtil;
import ongi.ongibe.util.DateUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlbumCacheService {

    private final UserAlbumRepository userAlbumRepository;
    private final SecurityUtil securityUtil;

    @Cacheable(value = "monthlyAlbum", key = "#userId + ':' + #yearMonth")
    public MonthlyAlbumResponseDTO getMonthlyAlbum(Long userId, String yearMonth) {
        User user = securityUtil.getCurrentUser();
        List<UserAlbum> userAlbumList = userAlbumRepository.findAllByUser(user);
        List<AlbumInfo> albumInfos = getAlbumInfos(userAlbumList, yearMonth);

        boolean hasNext = userAlbumRepository.existsByUserAndAlbum_CreatedAtBefore(user, DateUtil.getStartOfMonth(yearMonth));
        String nextYearMonth = hasNext ? DateUtil.getPreviousYearMonth(yearMonth) : null;

        return new MonthlyAlbumResponseDTO(albumInfos, nextYearMonth, hasNext);
    }

    private List<AlbumInfo> getAlbumInfos(List<UserAlbum> userAlbumList, String yearMonth) {
        LocalDateTime start = DateUtil.getStartOfMonth(yearMonth);
        LocalDateTime end = DateUtil.getEndOfMonth(yearMonth);

        return userAlbumList.stream()
                .map(UserAlbum::getAlbum)
                .filter(album -> !album.getCreatedAt().isBefore(start) && !album.getCreatedAt().isAfter(end))
                .map(MonthlyAlbumResponseDTO.AlbumInfo::of)
                .toList();
    }

    @CacheEvict(value = "monthlyAlbum", key = "#userId + ':' + #yearMonth")
    public void evictMonthlyAlbum(Long userId, String yearMonth) {}
}
