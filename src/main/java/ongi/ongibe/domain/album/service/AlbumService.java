package ongi.ongibe.domain.album.service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.UserAlbumRole;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.AlbumDetailResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumInviteResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumMemberResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumOwnerTransferResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumSummaryResponseDTO;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO.AlbumInfo;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.event.AlbumEvent;
import ongi.ongibe.domain.album.exception.AlbumException;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.album.repository.RedisInviteTokenRepository;
import ongi.ongibe.domain.place.entity.Place;
import ongi.ongibe.domain.album.entity.UserAlbum;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.security.util.SecurityUtil;
import ongi.ongibe.util.DateUtil;
import ongi.ongibe.util.JwtTokenProvider;
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
    private final PictureRepository pictureRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisInviteTokenRepository redisInviteTokenRepository;
    private final UserRepository userRepository;

    private static final String INVITE_LINK_PREFIX = "https://ongi.com/invite?token=";

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
        return BaseApiResponse.success("MONTHLY_ALBUM_SUCCESS", "앨범 조회 성공",monthlyAlbumResponseDTO);
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

        Map<Place, Picture> bestPictureOfPlace = getBestPictureOfPlace(album);

        List<AlbumSummaryResponseDTO> response = bestPictureOfPlace.values().stream()
                .map(Picture::toAlbumSummaryResponseDTO)
                .toList();

        return BaseApiResponse.success("ALBUM_SUMMARY_SUCCESS", "앨범 요약 조회 성공", response);
    }

    private Map<Place, Picture> getBestPictureOfPlace(Album album) {
        Map<Place, Picture> bestPictureOfPlace = new HashMap<>();

        for (Picture picture : album.getPictures()) {
            Place place = picture.getPlace();
            Picture currentBestPicture = bestPictureOfPlace.get(place);
            if (currentBestPicture == null || currentBestPicture.getQualityScore() < picture.getQualityScore()) {
                bestPictureOfPlace.put(place, picture);
            }
        }
        return bestPictureOfPlace;
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
        return BaseApiResponse.success("ALBUM_ACCESS_SUCCESS", "앨범 조회 성공", responseDTO);
    }

    private Album getAlbumIfMember(Long albumId) {
        Album album = getAlbum(albumId);
        validateAlbumMember(album, securityUtil.getCurrentUser().getId());
        return album;
    }

    private void validateAlbumMember(Album album, Long userId) {
        boolean isMember = album.getUserAlbums().stream()
                .anyMatch(ua -> ua.getUser().getId().equals(userId));
        if (!isMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "앨범 멤버가 아닙니다.");
        }
    }

    private Album getAlbum(Long albumId) {
        return albumRepository.findById(albumId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "앨범을 찾을 수 없습니다."));
    }

    @Transactional
    public Album createAlbum(String albumName, List<String> pictureUrls) {
        if (pictureUrls.size() > 100){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사진은 100장을 초과하여 추가할 수 없습니다");
        }
        User user = securityUtil.getCurrentUser();
        Album album = getEmptyAlbum(albumName);
        List<Picture> pictures = createPictures(pictureUrls, album, user);
        album.setPictures(pictures);
        associateAlbumWithUser(user, album);
        persistAlbum(album, pictures);
        eventPublisher.publishEvent(new AlbumEvent(album.getId(), pictureUrls));
        return album;
    }

    @Transactional
    public Album addPictures(Long albumId, List<String> pictureUrls) {
        Album album = getAlbumIfMember(albumId);

        List<Picture> existingPictures = album.getPictures();
        if (existingPictures == null) {
            existingPictures = new ArrayList<>();
            album.setPictures(existingPictures);
        }

        int previousSize = existingPictures.size();
        int newSize = previousSize + pictureUrls.size();

        checkAddPictureSize(newSize, previousSize);

        User user = securityUtil.getCurrentUser();
        List<Picture> newPictures = createPictures(pictureUrls, album, user);
        existingPictures.addAll(newPictures);

        albumRepository.save(album);
        eventPublisher.publishEvent(new AlbumEvent(albumId, pictureUrls));
        return album;
    }

    @Transactional
    public Album updateAlbumName(Long albumId, String albumName) {
        Album album = getAlbumIfMember(albumId);
        validAlbumOwner(album);
        album.setName(albumName);
        return albumRepository.save(album);
    }

    @Transactional
    public List<Picture> updatePicture(Long albumId, List<Long> pictureIds){
        Album album = getAlbumIfMember(albumId);
        validAlbumOwner(album);

        List<Picture> pictures = pictureRepository.findAllById(pictureIds).stream()
                .filter(p -> p.getAlbum().getId().equals(albumId))
                .toList();

        List<String> urls = pictures.stream()
                .map(Picture::getPictureURL)
                .toList();
        pictureRepository.markPicturesDuplicatedAsStable(urls);
        pictureRepository.markPicturesShakyAsStable(urls);
        return pictures;
    }

    @Transactional
    public void deletePictures(Long albumId, List<Long> pictureIds) {
        Album album = getAlbumIfMember(albumId);
        validAlbumOwner(album);

        List<Picture> pictures = pictureRepository.findAllById(pictureIds).stream()
                .filter(p -> p.getAlbum().getId().equals(albumId))
                .toList();

        if (pictures.size() != pictureIds.size()) {
            throw new AlbumException(HttpStatus.BAD_REQUEST, "삭제할 수 없는 사진이 포함되어 있습니다.");
        }

        for (Picture p : pictures) {
            p.setDeletedAt(LocalDateTime.now());
        }
    }

    @Transactional
    public void deleteAlbum(Long albumId) {
        Album album = getAlbumIfMember(albumId);
        validAlbumOwner(album);
        album.setDeletedAt(LocalDateTime.now());
        for (Picture p : album.getPictures()){
            p.setDeletedAt(LocalDateTime.now());
        }
    }

    private void checkAddPictureSize(int newSize, int previousSize) {
        if (newSize > 100) {
            int remaining = 100 - previousSize;
            throw new AlbumException(
                    HttpStatus.BAD_REQUEST,
                    "같은 앨범에 사진은 100장을 초과하여 추가할 수 없습니다. 추가 가능한 사진 수: " + remaining + "장"
            );
        }
    }

    private void persistAlbum(Album album, List<Picture> pictures) {
        albumRepository.save(album);
        pictureRepository.saveAll(pictures);
    }

    private void associateAlbumWithUser(User user, Album album) {
        UserAlbum userAlbum = UserAlbum.of(user, album, UserAlbumRole.OWNER);
        album.setUserAlbums(List.of(userAlbum));
    }

    private static List<Picture> createPictures(List<String> pictureUrls, Album album, User user) {
        return pictureUrls.stream()
                .map(url -> Picture.of(album, user, url))
                .toList();
    }

    private Album getEmptyAlbum(String albumName) {
        return Album.builder()
                .name(albumName)
                .userAlbums(new ArrayList<>())
                .pictures(new ArrayList<>())
                .build();
    }

    private void validAlbumOwner(Album album) {
        UserAlbum userAlbum = userAlbumRepository.findByUserAndAlbum(securityUtil.getCurrentUser(),
                album);
        if (!userAlbum.getRole().equals(UserAlbumRole.OWNER)) {
            throw new AlbumException(HttpStatus.FORBIDDEN, "소유자만 앨범 이름을 변경할 수 있습니다.");
        }
    }

    @Transactional
    public BaseApiResponse<String> createInviteToken(Long albumId){
        Album album = getAlbumIfMember(albumId);
        validAlbumOwner(album);

        String token = jwtTokenProvider.generateInviteToken(albumId);
        redisInviteTokenRepository.save(token, albumId);
        return BaseApiResponse.success("INVITE_LINK_CREATED", "초대 링크가 생성되었습니다.", INVITE_LINK_PREFIX + token);
    }

    @Transactional
    public BaseApiResponse<AlbumInviteResponseDTO> acceptInvite(String token){
        if (redisInviteTokenRepository.existsByToken(token)) {
            Long tokenAlbumId = jwtTokenProvider.validateAndExtractInviteId(token);
            Album album = getAlbum(tokenAlbumId);
            UserAlbum userAlbum = UserAlbum.of(securityUtil.getCurrentUser(), album, UserAlbumRole.NORMAL);
            userAlbumRepository.save(userAlbum);
            redisInviteTokenRepository.remove(token);
            AlbumInviteResponseDTO response = new AlbumInviteResponseDTO(tokenAlbumId,
                    album.getName());
            return BaseApiResponse.success("ALBUM_INVITE_SUCCESS", "앨범에 초대되었습니다.", response);
        }
        throw new AlbumException(HttpStatus.UNAUTHORIZED, "발급되지 않은 초대코드입니다.");
    }

    @Transactional
    public BaseApiResponse<AlbumOwnerTransferResponseDTO> transferAlbumOwner(Long albumId, Long newOwnerId){
        User oldOwner = securityUtil.getCurrentUser();
        User newUser = userRepository.findById(newOwnerId).orElseThrow(
                () -> new AlbumException(HttpStatus.NOT_FOUND, "이양할 유저 정보를 찾을 수 없습니다.")
        );
        UserAlbum oldUserAlbum = userAlbumRepository.findByUserAndAlbum(oldOwner, getAlbumIfMember(albumId));
        UserAlbum newUserAlbum = userAlbumRepository.findByUserAndAlbum(newUser, getAlbumIfMember(albumId));
        if (!oldUserAlbum.getRole().equals(UserAlbumRole.OWNER)) {
            throw new AlbumException(HttpStatus.FORBIDDEN, "현재 OWNER만 소유권을 위임할 수 있습니다.");
        }
        oldUserAlbum.setRole(UserAlbumRole.NORMAL);
        newUserAlbum.setRole(UserAlbumRole.OWNER);
        return BaseApiResponse.success(
                "ALBUM_OWNERSHIP_TRANSFER_SUCCESS", "앨범 소유권이 정상적으로 이전되었습니다.",
                new AlbumOwnerTransferResponseDTO(oldOwner.getId(), newUser.getId())
        );
    }

    @Transactional(readOnly = true)
    public BaseApiResponse<AlbumMemberResponseDTO> getAlbumMembers(Long albumId) {
        User user = securityUtil.getCurrentUser();
        Album album = getAlbumIfMember(albumId);
        List<UserAlbum> members = userAlbumRepository.findAllByAlbumAndUser(album, user);

        List<AlbumMemberResponseDTO.UserInfo> userInfos = members.stream()
                .map(ua -> new AlbumMemberResponseDTO.UserInfo(
                        ua.getUser().getId(),
                        ua.getUser().getNickname(),
                        ua.getRole(),
                        ua.getUser().getProfileImage()
                ))
                .toList();

        return BaseApiResponse.success(
                "ALBUM_MEMBER_LIST_SUCCESS",
                "공동작업자 목록 조회 성공",
                new AlbumMemberResponseDTO(userInfos));
    }
}
