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
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.cache.user.UserCacheService;
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

    private final UserRepository userRepository;
    private final PictureRepository pictureRepository;
    private final PresignedUrlService presignedUrlService;
    private final SecurityUtil securityUtil;
    private final UserCacheService userCacheService;

    @Transactional(readOnly = true)
    public BaseApiResponse<UserTotalStateResponseDTO> getUserTotalState(){
        User user = securityUtil.getCurrentUser();
        UserTotalStateResponseDTO userTotalStateResponseDTO = userCacheService.getUserTotalState(user);

        return BaseApiResponse.<UserTotalStateResponseDTO>builder()
                .code("USER_TOTAL_STATISTICS_SUCCESS")
                .message("유저 통계 조회 성공")
                .data(userTotalStateResponseDTO)
                .build();
    }

    @Transactional(readOnly = true)
    public BaseApiResponse<UserPictureStatResponseDTO> getUserPictureStat(String yearMonth){
        User user = securityUtil.getCurrentUser();
        UserPictureStatResponseDTO response = userCacheService.getUserPictureStat(user, yearMonth);
        return BaseApiResponse.success(
                "USER_IMAGE_STATISTICS_SUCCESS",
                "월간 일별 사진 업로드 수 조회 성공",
                response);
    }

    @Transactional(readOnly = true)
    public BaseApiResponse<UserPlaceStatResponseDTO> getUserPlaceStat(String yearMonth){
        User user = securityUtil.getCurrentUser();
        UserPlaceStatResponseDTO response = userCacheService.getUserPlaceStat(user, yearMonth);
        return BaseApiResponse.success("USER_PLACE_SUCCESS", "유저 방문 조회 성공", response);
    }

    @Transactional(readOnly = true)
    public BaseApiResponse<UserTagStatResponseDTO> getUserTagStat(String yearMonth){
        User user = securityUtil.getCurrentUser();
        UserTagStatResponseDTO response = userCacheService.getUserTagState(user, yearMonth);
        return BaseApiResponse.success("USER_TAG_STATISTICS_SUCCESS", "월별 최다기록 태그 및 사진 조회 성공", response);
    }

    @Transactional(readOnly = true)
    public BaseApiResponse<UserInfoResponseDTO> getUserInfo(Long userId){
        User user = getUserIfCorrectId(userId);
        UserInfoResponseDTO response = UserInfoResponseDTO.of(user);

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
        UserInfoResponseDTO response = new UserInfoResponseDTO(user.getId(), user.getNickname(), user.getProfileImage(), 300);
        return BaseApiResponse.success("USER_UPDATE_SUCCESS", "유저 정보 수정 완료했습니다.", response);
    }
}
