package ongi.ongibe.cache.user;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.album.repository.PlaceRepository;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.domain.user.dto.UserTagStatResponseDTO;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO.PictureCoordinate;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.cache.CacheKeyUtil;
import ongi.ongibe.global.cache.RedisCacheService;
import ongi.ongibe.util.DateUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCacheService {

    private final RedisCacheService redisCacheService;
    private final UserRepository userRepository;
    private final UserAlbumRepository userAlbumRepository;
    private final PictureRepository pictureRepository;
    private final PlaceRepository placeRepository;

    private static final Duration TTL = Duration.ofHours(12);

    public UserTotalStateResponseDTO getUserTotalState(User user) {
        String key = CacheKeyUtil.key("userTotalState", user.getId());
        return redisCacheService.get(key, UserTotalStateResponseDTO.class).orElseGet(() -> {
            UserTotalStateResponseDTO response = buildUserTotalStateResponse(user);
            redisCacheService.set(key, response, TTL);
            return response;
        });
    }

    private UserTotalStateResponseDTO buildUserTotalStateResponse(User user) {
        List<PictureCoordinate> coordinateList =
                pictureRepository.findAllByUser(user).stream()
                        .map(Picture::toPictureCoordinate)
                        .toList();
        int albumCount = userAlbumRepository.countByUser(user);
        int placeCount = placeRepository.countDistinctByPicturesByUser(user);

        return new UserTotalStateResponseDTO(coordinateList, albumCount, placeCount);
    }

    public void refreshUserTotalState(Long userId) {
        String key = CacheKeyUtil.key("userTotalState", userId);
        User user = userRepository.findById(userId).orElseThrow();
        UserTotalStateResponseDTO response = buildUserTotalStateResponse(user);
        redisCacheService.set(key, response, TTL);
    }

    public UserTagStatResponseDTO getUserTagState(User user, String yearMonth) {
        String key = CacheKeyUtil.key("userTagState", user.getId(), yearMonth);
        return redisCacheService.get(key, UserTagStatResponseDTO.class).orElseGet(() -> {
            UserTagStatResponseDTO response = buildUserTagStatResponse(user, yearMonth);
            redisCacheService.set(key, response, TTL);
            return response;
        });
    }

    private UserTagStatResponseDTO buildUserTagStatResponse(User user, String yearMonth) {
        LocalDateTime startDate = DateUtil.getStartOfMonth(yearMonth);
        LocalDateTime endDate = DateUtil.getEndOfMonth(yearMonth);
        List<Picture> pictures = pictureRepository.findAllByUserAndCreatedAtBetween(user, startDate, endDate);
        String maxTag = getMaxTag(pictures);
        if (maxTag == null) {
            return new UserTagStatResponseDTO(null, List.of());
        }
        List<String> pictureUrls = pictures.stream()
                .filter(p -> Objects.equals(p.getTag(), maxTag))
                .sorted(Comparator.comparing(Picture::getQualityScore).reversed())
                .map(Picture::getPictureURL)
                .limit(4)
                .toList();
        return new UserTagStatResponseDTO(maxTag, pictureUrls);
    }

    private static String getMaxTag(List<Picture> pictures) {
        Map<String, Long> tagCount = pictures.stream()
                .filter(p->p.getTag() != null && !p.getTag().isBlank() && !p.getTag().equals("기타"))
                .collect(Collectors.groupingBy(Picture::getTag, Collectors.counting()));
        return tagCount.entrySet().stream()
                .max(Entry.comparingByValue())
                .map(Entry::getKey)
                .orElse(null);
    }
}
