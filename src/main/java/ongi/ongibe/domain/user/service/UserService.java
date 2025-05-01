package ongi.ongibe.domain.user.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.album.repository.PlaceRepository;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.global.security.util.SecurityUtil;
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
}
