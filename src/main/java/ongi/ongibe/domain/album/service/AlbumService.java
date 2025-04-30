package ongi.ongibe.domain.album.service;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.common.ApiResponse;
import ongi.ongibe.domain.album.dto.AlbumDetailResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumSummaryResponseDTO;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO.AlbumInfo;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.entity.Place;
import ongi.ongibe.domain.album.entity.UserAlbum;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.security.config.CustomUserDetails;
import ongi.ongibe.global.security.util.SecurityUtil;
import ongi.ongibe.util.DateUtil;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties.Http;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumService {

    private final UserAlbumRepository userAlbumRepository;
    private final AlbumRepository albumRepository;
    private final SecurityUtil securityUtil;

    @Transactional(readOnly = true)
    public ApiResponse<MonthlyAlbumResponseDTO> getMonthlyAlbum(String yearMonth) {
        User user = securityUtil.getCurrentUser();
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

    @Transactional(readOnly = true)
    public ApiResponse<List<AlbumSummaryResponseDTO>> getAlbumSummary(Long albumId) {
        Album album = getAlbumifMember(albumId);

        Map<Place, Picture> bestPictureinPlace = new HashMap<>();

        for (Picture picture : album.getPictures()) {
            Place place = picture.getPlace();
            Picture currentBestPicture = bestPictureinPlace.get(place);
            if (currentBestPicture == null || currentBestPicture.getQualityScore() < picture.getQualityScore()) {
                bestPictureinPlace.put(place, picture);
            }
        }

        List<AlbumSummaryResponseDTO> response = bestPictureinPlace.values().stream()
                .map(pic -> AlbumSummaryResponseDTO.builder()
                        .pictureId(pic.getId())
                        .pictureURL(pic.getPictureURL())
                        .latitude(pic.getLatitude())
                        .longitude(pic.getLongitude())
                        .build())
                .toList();

        return ApiResponse.<List<AlbumSummaryResponseDTO>>builder()
                .code("ALBUM_SUMMARY_SUCCESS")
                .message("앨범 요약 조회 성공")
                .data(response)
                .build();
    }

    @Transactional(readOnly = true)
    public ApiResponse<AlbumDetailResponseDTO> getAlbumDetail(Long albumId) {
        Album album = getAlbumifMember(albumId);

        List<AlbumDetailResponseDTO.PictureInfo> pictureInfos = album.getPictures().stream()
                .map(p -> AlbumDetailResponseDTO.PictureInfo.builder()
                        .pictureId(p.getId())
                        .pictureURL(p.getPictureURL())
                        .latitude(p.getLatitude())
                        .longitude(p.getLongitude())
                        .tag(p.getTag())
                        .qualityScore(p.getQualityScore())
                        .isDuplicated(p.isDuplicated())
                        .isShaky(p.isShaky())
                        .takeAt(p.getTakeAt())
                        .build())
                .toList();

        AlbumDetailResponseDTO responseDTO = AlbumDetailResponseDTO.builder()
                .title(album.getName())
                .picture(pictureInfos)
                .build();

        return ApiResponse.<AlbumDetailResponseDTO>builder()
                .code("ALBUM_ACCESS_SUCCESS")
                .message("앨범 조회 성공")
                .data(responseDTO)
                .build();
    }

    private Album getAlbumifMember(Long albumId) {
        Album album = getAlbum(albumId);
        boolean isMember = album.getUserAlbums().stream()
                .anyMatch(ua -> ua.getUser().getId().equals(securityUtil.getCurrentUserId()));
        if (!isMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "앨범 멤버가 아닙니다.");
        }
        return album;
    }

    private Album getAlbum(Long albumId) {
        return albumRepository.findById(albumId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "앨범을 찾을 수 없습니다."));
    }

}
