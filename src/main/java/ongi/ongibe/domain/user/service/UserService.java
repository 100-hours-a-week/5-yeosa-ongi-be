package ongi.ongibe.domain.user.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.album.repository.PlaceRepository;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.domain.place.entity.Place;
import ongi.ongibe.domain.user.dto.UserPictureStatResponseDTO;
import ongi.ongibe.domain.user.dto.UserPlaceStatResponseDTO;
import ongi.ongibe.domain.user.dto.UserTagStatResponseDTO;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.global.security.util.SecurityUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PlaceRepository placeRepository;
    private final UserAlbumRepository userAlbumRepository;
    private final PictureRepository pictureRepository;
    private final SecurityUtil securityUtil;

    @Transactional(readOnly = true)
    public BaseApiResponse<UserTotalStateResponseDTO> getUserTotalState(){
        User user = securityUtil.getCurrentUser();

        List<UserTotalStateResponseDTO.PictureCoordinate> coordinateList =
                pictureRepository.findAllByUser(user).stream()
                        .map(Picture::toPictureCoordinate)
                        .toList();
        int albumCount = userAlbumRepository.countByUser(user);
        int placeCount = placeRepository.countDistinctByPicturesByUser(user);

        UserTotalStateResponseDTO userTotalStateResponseDTO =
                new UserTotalStateResponseDTO(coordinateList, albumCount, placeCount);

        return BaseApiResponse.<UserTotalStateResponseDTO>builder()
                .code("USER_TOTAL_STATISTICS_SUCCESS")
                .message("유저 통계 조회 성공")
                .data(userTotalStateResponseDTO)
                .build();
    }

    @Transactional(readOnly = true)
    public BaseApiResponse<UserPictureStatResponseDTO> getUserPictureStat(String yearMonth){
        User user = securityUtil.getCurrentUser();
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate startMonth = ym.atDay(1) ;
        LocalDate endMonth = ym.atEndOfMonth();
        List<Object[]> results = pictureRepository.countPicturesByDate(user.getId(), startMonth, endMonth);
        Map<String, Integer> dailyCountMap = new LinkedHashMap<>();
        for (int day = 1; day<=ym.lengthOfMonth(); day++){
            LocalDateTime date = ym.atDay(day).atStartOfDay();
            dailyCountMap.put(date.toString(), 0);
        }

        for (Object[] result : results){
            LocalDate date = (LocalDate) result[0];
            int count = (int) result[1];
            dailyCountMap.put(date.toString(), count);
        }

        UserPictureStatResponseDTO response = new UserPictureStatResponseDTO(yearMonth, dailyCountMap);
        return BaseApiResponse.success(
                "USER_IMAGE_STATISTICS_SUCCESS",
                "월간 일별 사진 업로드 수 조회 성공",
                response);
    }

    @Transactional(readOnly = true)
    public BaseApiResponse<UserPlaceStatResponseDTO> getUserPlaceStat(String yearMonth){
        User user = securityUtil.getCurrentUser();
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDateTime startDate = ym.atDay(1).atStartOfDay();
        LocalDateTime endDate = ym.atEndOfMonth().atTime(LocalTime.MAX);
        List<Object[]> topPlace = pictureRepository.mostVisitPlace(
                user.getId(), startDate, endDate, PageRequest.of(0,1));
        UserPlaceStatResponseDTO response;
        if (topPlace.isEmpty()){
            response = new UserPlaceStatResponseDTO(null, null, null, List.of());
            return BaseApiResponse.success("USER_PLACE_SUCCESS", "유저 방문 조회 성공", response);
        }
        String city = topPlace.getFirst()[0].toString();
        String district = topPlace.getFirst()[1].toString();
        String town = topPlace.getFirst()[2].toString();

        List<String> tags = getTopTags(user, city, district, town, startDate, endDate);
        response = new UserPlaceStatResponseDTO(city, district, town, tags);
        return BaseApiResponse.success("USER_PLACE_SUCCESS", "유저 방문 조회 성공", response);
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

    @Transactional(readOnly = true)
    public BaseApiResponse<UserTagStatResponseDTO> getUserTagStat(String yearMonth){
        User user = securityUtil.getCurrentUser();
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDateTime startDate = ym.atDay(1).atStartOfDay();
        LocalDateTime endDate = ym.atEndOfMonth().atTime(LocalTime.MAX);
        List<Picture> pictures = pictureRepository.findAllByUserAndCreatedAtBetween(user, startDate, endDate);
        String maxTag = getMaxTag(pictures);
        if (maxTag == null){
            return BaseApiResponse.success("USER_TAG_STATISTICS_SUCCESS", "월별 최다기록 태그 및 사진 조회 성공",
                    new UserTagStatResponseDTO(null, List.of()));
        }
        List<String> pictureUrls = pictures.stream()
                .filter(p -> p.getTag().equals(maxTag))
                .sorted(Comparator.comparing(Picture::getQualityScore).reversed())
                .map(Picture::getPictureURL)
                .limit(4)
                .toList();
        UserTagStatResponseDTO response = new UserTagStatResponseDTO(maxTag, pictureUrls);
        return BaseApiResponse.success("USER_TAG_STATISTICS_SUCCESS", "월별 최다기록 태그 및 사진 조회 성공", response);
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
