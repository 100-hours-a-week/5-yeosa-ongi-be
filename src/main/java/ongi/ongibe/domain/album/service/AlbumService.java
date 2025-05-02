package ongi.ongibe.domain.album.service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.UserAlbumRole;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.AlbumDetailResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumSummaryResponseDTO;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO.AlbumInfo;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.event.AlbumCreatedEvent;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.place.entity.Place;
import ongi.ongibe.domain.album.entity.UserAlbum;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.global.security.util.SecurityUtil;
import ongi.ongibe.util.DateUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
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
    private final AlbumProcessService albumProcessService;
    private final PictureRepository pictureRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public BaseApiResponse<MonthlyAlbumResponseDTO> getMonthlyAlbum(String yearMonth) {
        User user = securityUtil.getCurrentUser();
        List<UserAlbum> userAlbumList = userAlbumRepository.findAllByUser(user);
        List<AlbumInfo> albumInfos = getAlbumInfos(userAlbumList, yearMonth);

        boolean hasNext = userAlbumRepository.existsByUserAndAlbum_CreatedAtBefore(user, DateUtil.getStartOfMonth(yearMonth));
        String nextYearMonth = hasNext ? DateUtil.getPreviousYearMonth(yearMonth) : null;
        MonthlyAlbumResponseDTO monthlyAlbumResponseDTO = new MonthlyAlbumResponseDTO(
                albumInfos,
                nextYearMonth,
                hasNext
        );
        return BaseApiResponse.<MonthlyAlbumResponseDTO>builder()
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
                .map(MonthlyAlbumResponseDTO.AlbumInfo::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public BaseApiResponse<List<AlbumSummaryResponseDTO>> getAlbumSummary(Long albumId) {
        Album album = getAlbumIfMember(albumId);

        Map<Place, Picture> bestPictureinPlace = getBestPictureofPlace(album);

        List<AlbumSummaryResponseDTO> response = bestPictureinPlace.values().stream()
                .map(Picture::toAlbumSummaryResponseDTO)
                .toList();

        return BaseApiResponse.<List<AlbumSummaryResponseDTO>>builder()
                .code("ALBUM_SUMMARY_SUCCESS")
                .message("앨범 요약 조회 성공")
                .data(response)
                .build();
    }

    private Map<Place, Picture> getBestPictureofPlace(Album album) {
        Map<Place, Picture> bestPictureofPlace = new HashMap<>();

        for (Picture picture : album.getPictures()) {
            Place place = picture.getPlace();
            Picture currentBestPicture = bestPictureofPlace.get(place);
            if (currentBestPicture == null || currentBestPicture.getQualityScore() < picture.getQualityScore()) {
                bestPictureofPlace.put(place, picture);
            }
        }
        return bestPictureofPlace;
    }

    @Transactional(readOnly = true)
    public BaseApiResponse<AlbumDetailResponseDTO> getAlbumDetail(Long albumId) {
        Album album = getAlbumIfMember(albumId);

        List<AlbumDetailResponseDTO.PictureInfo> pictureInfos = album.getPictures().stream()
                .map(Picture::toPictureInfo)
                .toList();

        AlbumDetailResponseDTO responseDTO = new AlbumDetailResponseDTO(
                album.getName(),
                pictureInfos
        );
        return BaseApiResponse.<AlbumDetailResponseDTO>builder()
                .code("ALBUM_ACCESS_SUCCESS")
                .message("앨범 조회 성공")
                .data(responseDTO)
                .build();
    }

    private Album getAlbumIfMember(Long albumId) {
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

    @Transactional
    public Album createAlbum(String albumName, List<String> pictureUrls) {
        log.info("albumName={}, pictureUrls={}", albumName, pictureUrls);
        User user = securityUtil.getCurrentUser();
        Album album = Album.builder()
                .name(albumName)
                .userAlbums(new ArrayList<>())
                .pictures(new ArrayList<>())
                .build();
        log.info("album={}", album.getName());
        List<Picture> pictures = pictureUrls.stream()
                .map(url -> Picture.of(album, user, url))
                .toList();
        album.setPictures(pictures);
        pictures.stream().map(Picture::getPictureURL).forEach(log::info);
        UserAlbum userAlbum = UserAlbum.of(user, album, UserAlbumRole.OWNER);
        album.setUserAlbums(List.of(userAlbum));
        albumRepository.save(album);
        pictureRepository.saveAll(pictures);
        eventPublisher.publishEvent(new AlbumCreatedEvent(album.getId()));
        return album;
    }

}
