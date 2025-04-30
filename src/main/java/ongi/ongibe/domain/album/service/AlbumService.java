package ongi.ongibe.domain.album.service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.common.ApiResponse;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO.AlbumInfo;
import ongi.ongibe.domain.album.entity.UserAlbum;
import ongi.ongibe.domain.album.entity.UserAlbumRepository;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.security.config.CustomUserDetails;
import ongi.ongibe.util.DateUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumService {

    private final UserRepository userRepository;
    private final UserAlbumRepository userAlbumRepository;

    @Transactional
    public ApiResponse<MonthlyAlbumResponseDTO> getMonthlyAlbum(String yearMonth) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.");
        }

        Long userId = userDetails.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다.")
                );

        List<UserAlbum> userAlbumList = userAlbumRepository.findAllByUser(user);
        List<AlbumInfo> albumInfos = getAlbumInfos(userAlbumList, yearMonth);

        boolean hasNext = userAlbumRepository.existsByUserAndAlbum_CreatedAtBefore(user, DateUtil.getStartOfMonth(yearMonth));
        String nextYearMonth = hasNext ? DateUtil.getPreviousYearMonth(yearMonth) : null;
        MonthlyAlbumResponseDTO monthlyAlbumResponseDTO = MonthlyAlbumResponseDTO.builder()
                .albumInfo(albumInfos)
                .nextYearMonth(nextYearMonth)
                .hasNext(hasNext)
                .build();
        return ApiResponse.<MonthlyAlbumResponseDTO>builder()
                .code("MONTHLY_ALBUM_SUCCESS")
                .message("앨범 조회 성공")
                .data(monthlyAlbumResponseDTO)
                .build();
    }

    private List<AlbumInfo> getAlbumInfos(List<UserAlbum> userAlbumList,
            String yearMonth) {
        LocalDateTime startOfMonth = DateUtil.getStartOfMonth(yearMonth);
        LocalDateTime endOfMonth = DateUtil.getEndOfMonth(yearMonth);
        return userAlbumList.stream()
                .map(UserAlbum::getAlbum)
                .filter(album -> album.getCreatedAt().isAfter(startOfMonth.minusNanos(1)) &&
                        album.getCreatedAt().isBefore(endOfMonth.plusNanos(1)))
                .map(album -> AlbumInfo.builder()
                        .albumId(album.getId())
                        .albumName(album.getName())
                        .thumbnailPictureURL(album.getThumbnailPicture() != null ? album.getThumbnailPicture().getPictureURL() : null)
                        .createdAt(album.getCreatedAt())
                        .memberProfileImageURL(
                                album.getUserAlbums().stream()
                                        .map(ua -> ua.getUser().getProfileImage())
                                        .toList()
                        )
                        .build())
                .toList();
    }

}
