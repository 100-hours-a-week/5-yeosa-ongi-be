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
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.album.repository.PlaceRepository;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.domain.user.dto.UserPictureStatResponseDTO;
import ongi.ongibe.domain.user.dto.UserPlaceStatResponseDTO;
import ongi.ongibe.domain.user.dto.UserTagStatResponseDTO;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO.PictureCoordinate;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.cache.CacheKeyUtil;
import ongi.ongibe.global.cache.RedisCacheService;
import ongi.ongibe.util.DateUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCacheService {

    private final RedisCacheService redisCacheService;
    private final UserAlbumRepository userAlbumRepository;
    private final PictureRepository pictureRepository;
    private final PlaceRepository placeRepository;

    private static final Duration TTL = Duration.ofHours(12);

    public UserTotalStateResponseDTO getUserTotalState(User user) {
        String key = CacheKeyUtil.key("userTotalStat", user.getId());
        return redisCacheService.get(key, UserTotalStateResponseDTO.class).orElseGet(() -> {
            List<PictureCoordinate> coordinateList =
                    pictureRepository.findAllByUser(user).stream()
                            .map(Picture::toPictureCoordinate)
                            .toList();
            int albumCount = userAlbumRepository.countByUser(user);
            int placeCount = placeRepository.countDistinctByPicturesByUser(user);

            UserTotalStateResponseDTO response =
                    new UserTotalStateResponseDTO(coordinateList, albumCount, placeCount);
            redisCacheService.set(key, response, TTL);
            return response;
        });
    }

    public UserTagStatResponseDTO getUserTagStat(User user, String requestYearMonth) {
        String yearMonth = String.valueOf(DateUtil.parseOrNow(requestYearMonth));
        String key = CacheKeyUtil.key("userTagStat", user.getId(), yearMonth);
        return redisCacheService.get(key, UserTagStatResponseDTO.class).orElseGet(() -> {
            LocalDateTime startDate = DateUtil.getStartOfMonth(yearMonth);
            LocalDateTime endDate = DateUtil.getEndOfMonth(yearMonth);
            List<Picture> pictures = pictureRepository.findAllByUserAndCreatedAtBetween(user, startDate, endDate);
            String maxTag = getMaxTag(pictures);
            UserTagStatResponseDTO response;
            if (maxTag == null){
                response = new UserTagStatResponseDTO(null, List.of());
                redisCacheService.set(key, response, TTL);
                return response;
            }
            List<String> pictureUrls = pictures.stream()
                    .filter(p -> Objects.equals(p.getTag(), maxTag))
                    .sorted(Comparator.comparing(Picture::getQualityScore).reversed())
                    .map(Picture::getPictureURL)
                    .limit(4)
                    .toList();
            response = new UserTagStatResponseDTO(maxTag, pictureUrls);
            redisCacheService.set(key, response, TTL);
            return response;
        });
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

    public UserPictureStatResponseDTO getUserPictureStat(User user, String requestYearMonth) {
        String yearMonth = String.valueOf(DateUtil.parseOrNow(requestYearMonth));
        String key = CacheKeyUtil.key("userPictureStat", user.getId(), yearMonth);
        return redisCacheService.get(key, UserPictureStatResponseDTO.class).orElseGet(() -> {
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
            UserPictureStatResponseDTO response = new UserPictureStatResponseDTO(yearMonth, responseMap);
            redisCacheService.set(key, response, TTL);
            return response;
        });
    }

    public UserPlaceStatResponseDTO getUserPlaceStat(User user, String requestYearMonth) {
        String yearMonth = String.valueOf(DateUtil.parseOrNow(requestYearMonth));
        String key = CacheKeyUtil.key("userPlaceStat", user.getId(), yearMonth);
        return redisCacheService.get(key, UserPlaceStatResponseDTO.class).orElseGet(() -> {
            LocalDateTime startDate = DateUtil.getStartOfMonth(yearMonth);
            LocalDateTime endDate = DateUtil.getEndOfMonth(yearMonth);
            List<Object[]> topPlace = pictureRepository.mostVisitPlace(
                    user.getId(), startDate, endDate, PageRequest.of(0,1));
            UserPlaceStatResponseDTO response;
            if (topPlace.isEmpty()){
                response = new UserPlaceStatResponseDTO(null, null, null, List.of());
                redisCacheService.set(key, response, TTL);
                return response;
            }
            String city = topPlace.getFirst()[0].toString();
            String district = topPlace.getFirst()[1].toString();
            String town = topPlace.getFirst()[2].toString();

            List<String> tags = getTopTags(user, city, district, town, startDate, endDate);
            response = new UserPlaceStatResponseDTO(city, district, town, tags);
            redisCacheService.set(key, response, TTL);
            return response;
        });
    }

    private List<String> getTopTags(User user, String city, String district, String town,
            LocalDateTime startDate, LocalDateTime endDate) {
        List<Picture> pictures = pictureRepository.findByUserAndPlaceAndCreatedAtBetween(
                user, city, district, town, startDate, endDate);
        Map<String, Integer> tagMap = new HashMap<>();
        for (Picture picture : pictures){
            String tag = picture.getTag();
            if (tag != null && !tag.isBlank() && !tag.equals("기타")){
                tagMap.put(tag, tagMap.getOrDefault(tag, 0) + 1);
            }
        }
        return tagMap.entrySet().stream()
                .sorted(Entry.<String, Integer>comparingByValue().reversed())
                .limit(6)
                .map(Entry::getKey)
                .toList();
    }
}
