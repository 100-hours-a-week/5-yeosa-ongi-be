package ongi.ongibe.domain.user.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.ApiResponse;
import ongi.ongibe.domain.album.entity.Place;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.album.repository.PlaceRepository;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.domain.album.service.AlbumService;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.security.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PlaceRepository placeRepository;
    private final UserAlbumRepository userAlbumRepository;
    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final SecurityUtil securityUtil;

    @Transactional(readOnly = true)
    public ApiResponse<UserTotalStateResponseDTO> getUserTotalState(){
        User user = securityUtil.getCurrentUser();

        List<UserTotalStateResponseDTO.PictureCoordinate> coordinateList =
                pictureRepository.findAllByUser(user).stream()
                        .map(p -> UserTotalStateResponseDTO.PictureCoordinate.builder()
                                .latitude(p.getLatitude())
                                .longitude(p.getLongitude())
                                .build())
                        .toList();
        int albumCount = userAlbumRepository.countByUser(user);
        int placeCount = placeRepository.countDistinctByPicturesByUser(user);

        UserTotalStateResponseDTO userTotalStateResponseDTO = UserTotalStateResponseDTO.builder()
                .albumCount(albumCount)
                .placeCount(placeCount)
                .pictureCoordinates(coordinateList)
                .build();

        return ApiResponse.<UserTotalStateResponseDTO>builder()
                .code("USER_TOTAL_STATISTICS_SUCCESS")
                .message("유저 통계 조회 성공")
                .data(userTotalStateResponseDTO)
                .build();
    }
}
