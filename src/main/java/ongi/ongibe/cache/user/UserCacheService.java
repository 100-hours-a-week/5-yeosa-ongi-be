package ongi.ongibe.cache.user;

import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import ongi.ongibe.domain.user.dto.UserPictureStatResponseDTO;
import ongi.ongibe.domain.user.dto.UserPlaceStatResponseDTO;
import ongi.ongibe.domain.user.dto.UserPlaceStatResponseDTO.TagCountDTO;
import ongi.ongibe.domain.user.dto.UserTagStatResponseDTO;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO.PictureCoordinate;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.cache.CacheKeyUtil;
import ongi.ongibe.global.cache.RedisCacheService;
import ongi.ongibe.global.util.DateUtil;
import org.springframework.data.domain.PageRequest;
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

    private static final Duration TTL = Duration.ofSeconds(5);

    public UserTotalStateResponseDTO getUserTotalState(User user) {
        String key = CacheKeyUtil.key("userTotalStat", user.getId());
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

    public void refreshUserTotalState(User user) {
        String key = CacheKeyUtil.key("userTotalStat", user.getId());
        UserTotalStateResponseDTO response = buildUserTotalStateResponse(user);
        redisCacheService.set(key, response, TTL);
    }

    public UserTagStatResponseDTO getUserTagState(User user, String yearMonth) {
        String key = CacheKeyUtil.key("userTagStat", user.getId(), yearMonth);
        return redisCacheService.get(key, UserTagStatResponseDTO.class).orElseGet(() -> {
            UserTagStatResponseDTO response = buildUserTagStatResponse(user, yearMonth);
            redisCacheService.set(key, response, TTL);
            return response;
        });
    }

    public void refreshUserTagState(User user, String yearMonth) {
        String key = CacheKeyUtil.key("userTagStat", user.getId(), yearMonth);
        UserTagStatResponseDTO response = buildUserTagStatResponse(user, yearMonth);
        redisCacheService.set(key, response, TTL);
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
                .filter(p->p.getTag() != null && !p.getTag().isBlank() && !p.getTag().equals("기타") && !p.getTag().equals("AI 분석 전"))
                .collect(Collectors.groupingBy(Picture::getTag, Collectors.counting()));
        return tagCount.entrySet().stream()
                .max(Entry.comparingByValue())
                .map(Entry::getKey)
                .orElse(null);
    }

    public UserPictureStatResponseDTO getUserPictureStat(User user, String yearMonth) {
        String key = CacheKeyUtil.key("userPictureStat", user.getId(), yearMonth);
        return redisCacheService.get(key, UserPictureStatResponseDTO.class).orElseGet(() -> {
            UserPictureStatResponseDTO response = buildUserPictureStatResponse(user, yearMonth);
            redisCacheService.set(key, response, TTL);
            return response;
        });
    }

    private UserPictureStatResponseDTO buildUserPictureStatResponse(User user, String yearMonth) {
        YearMonth ym = DateUtil.parseOrNow(yearMonth);
        LocalDate startMonth = DateUtil.getStartOfMonth(yearMonth).toLocalDate();
        LocalDate endMonth = DateUtil.getEndOfMonth(yearMonth).toLocalDate();
        List<Object[]> results = pictureRepository.countPicturesByDate(user.getId(), startMonth, endMonth);
        Map<LocalDate, Integer> dailyCountMap = new LinkedHashMap<>();
        for (int day = 1; day<=ym.lengthOfMonth(); day++){
            dailyCountMap.put(ym.atDay(day), 0);
        }

        for (Object[] result : results){
            LocalDate date = ((Date) result[0]).toLocalDate();
            int count =((Number) result[1]).intValue();
            dailyCountMap.put(date, count);
        }

        Map<String, Integer> responseMap = dailyCountMap.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(), // LocalDate → String
                        Map.Entry::getValue,
                        (v1, v2) -> v1,
                        LinkedHashMap::new // 순서 유지
                ));
        return new UserPictureStatResponseDTO(yearMonth, responseMap);
    }

    public void refreshUserPictureStat(User user, String yearMonth) {
        String key = CacheKeyUtil.key("userPictureStat", user.getId(), yearMonth);
        UserPictureStatResponseDTO response = buildUserPictureStatResponse(user, yearMonth);
        redisCacheService.set(key, response, TTL);
    }

    public UserPlaceStatResponseDTO getUserPlaceStat(User user, String yearMonth) {
        String key = CacheKeyUtil.key("userPlaceStat", user.getId(), yearMonth);
        return redisCacheService.get(key, UserPlaceStatResponseDTO.class).orElseGet(() -> {
            UserPlaceStatResponseDTO response = buildUserPlaceStatResponse(user, yearMonth);
            redisCacheService.set(key, response, TTL);
            return response;
        });
    }

    private UserPlaceStatResponseDTO buildUserPlaceStatResponse(User user, String yearMonth) {
        LocalDateTime startDate = DateUtil.getStartOfMonth(yearMonth);
        LocalDateTime endDate = DateUtil.getEndOfMonth(yearMonth);
        List<Object[]> topPlace = pictureRepository.mostVisitPlace(
                user.getId(), startDate, endDate, PageRequest.of(0,1));
        if (topPlace.isEmpty()){
            return new UserPlaceStatResponseDTO(null, null, null, List.of());
        }
        String city = topPlace.getFirst()[0].toString();
        String district = topPlace.getFirst()[1].toString();
        String town = topPlace.getFirst()[2].toString();

        List<UserPlaceStatResponseDTO.TagCountDTO> tags = getTopTags(user, city, district, town, startDate, endDate);
        return new UserPlaceStatResponseDTO(city, district, town, tags);
    }

    private List<UserPlaceStatResponseDTO.TagCountDTO> getTopTags(User user, String city, String district, String town,
            LocalDateTime startDate, LocalDateTime endDate) {
        List<Picture> pictures = pictureRepository.findByUserAndPlaceAndCreatedAtBetween(
                user, city, district, town, startDate, endDate);
        Map<String, Integer> tagMap = new HashMap<>();
        for (Picture picture : pictures){
            String tag = picture.getTag();
            if (tag != null && !tag.isBlank()){
                tagMap.put(tag, tagMap.getOrDefault(tag, 0) + 1);
            }
        }
        return tagMap.entrySet().stream()
                .sorted(Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(entry -> new TagCountDTO(entry.getKey(), entry.getValue()))
                .toList();
    }

    public void refreshUserPlaceStat(User user, String yearMonth) {
        String key = CacheKeyUtil.key("userPlaceStat", user.getId(), yearMonth);
        UserPlaceStatResponseDTO response = buildUserPlaceStatResponse(user, yearMonth);
        redisCacheService.set(key, response, TTL);
    }
}
