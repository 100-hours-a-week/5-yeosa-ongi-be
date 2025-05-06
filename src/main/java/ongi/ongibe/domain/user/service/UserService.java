package ongi.ongibe.domain.user.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.album.repository.PlaceRepository;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.domain.user.dto.UserImageStatResponseDTO;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.global.security.util.SecurityUtil;
import ongi.ongibe.util.DateUtil;
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
    public BaseApiResponse<UserImageStatResponseDTO> getUserPlaceStat(String yearMonth){
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

        UserImageStatResponseDTO response = new UserImageStatResponseDTO(yearMonth, dailyCountMap);
        return BaseApiResponse.success(
                "USER_IMAGE_STATISTICS_SUCCESS",
                "월간 일별 사진 업로드 수 조회 성공",
                response);
    }
}
