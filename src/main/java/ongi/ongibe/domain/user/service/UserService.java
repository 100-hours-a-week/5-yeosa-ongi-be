package ongi.ongibe.domain.user.service;

import java.sql.Date;
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
import ongi.ongibe.domain.album.dto.UserUpdateRequestDTO;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.album.repository.PlaceRepository;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.domain.user.dto.UserInfoResponseDTO;
import ongi.ongibe.domain.user.dto.UserPictureStatResponseDTO;
import ongi.ongibe.domain.user.dto.UserPlaceStatResponseDTO;
import ongi.ongibe.domain.user.dto.UserTagStatResponseDTO;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.exception.UserException;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.s3.PresignedUrlService;
import ongi.ongibe.global.security.util.SecurityUtil;
import ongi.ongibe.util.DateUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PlaceRepository placeRepository;
    private final UserAlbumRepository userAlbumRepository;
    private final UserRepository userRepository;
    private final PictureRepository pictureRepository;
    private final PresignedUrlService presignedUrlService;
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
        return BaseApiResponse.success(
                "USER_IMAGE_STATISTICS_SUCCESS",
                "월간 일별 사진 업로드 수 조회 성공",
                response);
    }

    @Transactional(readOnly = true)
    public BaseApiResponse<UserPlaceStatResponseDTO> getUserPlaceStat(String yearMonth){
        User user = securityUtil.getCurrentUser();
        LocalDateTime startDate = DateUtil.getStartOfMonth(yearMonth);
        LocalDateTime endDate = DateUtil.getEndOfMonth(yearMonth);
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
        LocalDateTime startDate = DateUtil.getStartOfMonth(yearMonth);
        LocalDateTime endDate = DateUtil.getEndOfMonth(yearMonth);
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

    @Transactional
    public BaseApiResponse<UserInfoResponseDTO> getUserInfo(Long userId){
        User user = getUserIfCorrectId(userId);
        UserInfoResponseDTO original = UserInfoResponseDTO.of(user);
        String rawUrl = original.profileImageURL();
        String finalUrl = rawUrl;
        if (rawUrl != null && rawUrl.contains("amazonaws.com")) {
            if (user.getS3Key() == null){
                String key = presignedUrlService.extractS3Key(rawUrl);
                user.setS3Key(key);
                userRepository.save(user);
            }
            finalUrl = presignedUrlService.generateGetPresignedUrl(user.getS3Key());
        }

        UserInfoResponseDTO response = new UserInfoResponseDTO(
                original.userId(),
                original.nickname(),
                finalUrl,
                original.cacheTil()
        );

        return BaseApiResponse.success("USER_INFO_SUCCESS", "유저 조회 완료했습니다.", response);
    }

    private User getUserIfCorrectId(Long userId) {
        User user = securityUtil.getCurrentUser();
        if (!userId.equals(user.getId())){
            throw new UserException(HttpStatus.BAD_REQUEST, "요청하는 유저가 본인이 아닙니다.");
        }
        return user;
    }

    @Transactional
    public BaseApiResponse<UserInfoResponseDTO> updateUserInfo(Long userId, UserUpdateRequestDTO request){
        User user = getUserIfCorrectId(userId);
        user.setNickname(request.nickname());
        user.setProfileImage(request.profileImageURL());
        String key = presignedUrlService.extractS3Key(request.profileImageURL());
        user.setS3Key(key);
        userRepository.save(user);
        UserInfoResponseDTO response = UserInfoResponseDTO.of(user);
        return BaseApiResponse.success("USER_UPDATE_SUCCESS", "유저 정보 수정 완료했습니다.", response);
    }
}
