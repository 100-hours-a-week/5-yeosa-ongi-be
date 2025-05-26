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
import ongi.ongibe.domain.album.AlbumProcessState;
import ongi.ongibe.domain.album.dto.AlbumCreateRequestGeoFrontDTO;
import ongi.ongibe.domain.album.dto.AlbumCreateRequestGeoFrontDTO.PictureRequestDTO;
import ongi.ongibe.domain.album.dto.AlbumDetailResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumInviteResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumMemberResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumOwnerTransferResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumPictureAddRequestGeoFrontDTO;
import ongi.ongibe.domain.album.dto.AlbumRoleResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumSummaryResponseDTO;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO.AlbumInfo;
import ongi.ongibe.domain.album.dto.PictureUrlCoordinateDTO;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.event.AlbumEvent;
import ongi.ongibe.domain.album.exception.AlbumException;
import ongi.ongibe.domain.album.factory.AlbumInfoFactory;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.album.repository.RedisInviteTokenRepository;
import ongi.ongibe.domain.notification.event.AlbumCreatedNotificationEvent;
import ongi.ongibe.domain.notification.event.InviteMemberNotificationEvent;
import ongi.ongibe.domain.place.entity.Place;
import ongi.ongibe.domain.album.entity.UserAlbum;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.s3.PresignedUrlService;
import ongi.ongibe.global.security.util.SecurityUtil;
import ongi.ongibe.util.DateUtil;
import ongi.ongibe.util.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
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
    private final PresignedUrlService presignedUrlService;
    private final AlbumInfoFactory albumInfoFactory;

    @Value("${custom.isProd}")
    private boolean isProd;

    private static final String INVITE_LINK_PREFIX_PROD = "https://ongi.today/invite?token=";
    private static final String INVITE_LINK_PREFIX_DEV = "https://dev.ongi.today/invite?token=";
    private static final int MAX_PICTURE_SIZE = 30;

    @Transactional
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
                .map(albumInfoFactory::from)
                .toList();
    }

    @Transactional
    public BaseApiResponse<List<AlbumSummaryResponseDTO>> getAlbumSummary(Long albumId) {
        Album album = getAlbumIfMember(albumId);

        Map<Place, Picture> bestPictureOfPlace = getBestPictureOfPlace(album);

        List<AlbumSummaryResponseDTO> response = bestPictureOfPlace.values().stream()
                .map(picture -> {
                    String key = picture.getS3Key() != null ? picture.getS3Key() : presignedUrlService.extractS3Key(picture.getPictureURL());
                    if (picture.getS3Key() == null) {
                        picture.setS3Key(key);
                        pictureRepository.save(picture);
                    }
                    String presignedUrl = presignedUrlService.generateGetPresignedUrl(key);
                    return picture.toAlbumSummaryResponseDTO(presignedUrl);
                })
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

    @Transactional
    public BaseApiResponse<AlbumDetailResponseDTO> getAlbumDetail(Long albumId) {
        Album album = getAlbumIfMember(albumId);
        log.info(presignedUrlService.generateGetPresignedUrl("KakaoTalk_Photo_2025-02-17-14-16-38 008.jpeg"));
        List<AlbumDetailResponseDTO.PictureInfo> pictureInfos = album.getPictures().stream()
                .map(picture -> {
                    String key = picture.getS3Key() != null ?
                            picture.getS3Key() : presignedUrlService.extractS3Key(picture.getPictureURL());
                    picture.setS3Key(key);
                    pictureRepository.save(picture);
                    System.out.println("DEBUG 진입: key = " + key);
                    String presignedUrl = presignedUrlService.generateGetPresignedUrl(key);
                    return picture.toPictureInfo(presignedUrl);
                })
                .toList();

        AlbumDetailResponseDTO responseDTO = new AlbumDetailResponseDTO(
                album.getName(),
                pictureInfos
        );
        return BaseApiResponse.success("ALBUM_ACCESS_SUCCESS", "앨범 조회 성공", responseDTO);
    }

    protected Album getAlbumIfMember(Long albumId) {
        Album album = getAlbum(albumId);
        if (!validateAlbumMember(album, securityUtil.getCurrentUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "앨범 멤버가 아닙니다.");
        };
        return album;
    }

    private boolean validateAlbumMember(Album album, Long userId) {
        return album.getUserAlbums().stream()
                .anyMatch(ua -> ua.getUser().getId().equals(userId));
    }

    protected Album getAlbum(Long albumId) {
        return albumRepository.findById(albumId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "앨범을 찾을 수 없습니다."));
    }

    @Transactional
    public void createAlbum(String albumName, List<? extends PictureUrlCoordinateDTO> pictureDTOs) {
        if (pictureDTOs.size() > MAX_PICTURE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사진은 10장을 초과하여 추가할 수 없습니다");
        }
        User user = securityUtil.getCurrentUser();
        Album album = getEmptyAlbum(albumName);
        List<Picture> pictures = createPictures(pictureDTOs, album, user);
        album.setPictures(pictures);
        album.setThumbnailPicture(pictures.getFirst());

        associateAlbumWithUser(user, album);
        persistAlbum(album, pictures);

        List<String> pictureUrls = pictureDTOs.stream()
                .map(PictureUrlCoordinateDTO::pictureUrl)
                .toList();

        eventPublisher.publishEvent(new AlbumCreatedNotificationEvent(album.getId(), user.getId()));
        eventPublisher.publishEvent(new AlbumEvent(album.getId(), pictureUrls));
    }
//    public void createAlbum(String albumName, List<String> pictureUrls) {
//        if (pictureUrls.size() > 100){
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사진은 100장을 초과하여 추가할 수 없습니다");
//        }
//        User user = securityUtil.getCurrentUser();
//        Album album = getEmptyAlbum(albumName);
//        List<Picture> pictures = createPictures(pictureUrls, album, user);
//        album.setPictures(pictures);
//        album.setThumbnailPicture(pictures.getFirst());
//        associateAlbumWithUser(user, album);
//        persistAlbum(album, pictures);
//        eventPublisher.publishEvent(new AlbumEvent(album.getId(), pictureUrls));
//    }

    @Transactional
    public void addPictures(Long albumId, List<? extends PictureUrlCoordinateDTO> pictureUrls) {
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

        List<String> pictures = pictureUrls.stream()
                .map(PictureUrlCoordinateDTO::pictureUrl)
                .toList();

        eventPublisher.publishEvent(new AlbumEvent(albumId, pictures));
    }

    @Transactional
    public void updateAlbumName(Long albumId, String albumName) {
        Album album = getAlbumIfMember(albumId);
        validAlbumOwner(album);
        album.setName(albumName);
        albumRepository.save(album);
    }

    @Transactional
    public void updatePicture(Long albumId, List<Long> pictureIds){
        Album album = getAlbumIfMember(albumId);
        validAlbumOwner(album);

        pictureRepository.markPicturesDuplicatedAsStable(albumId, pictureIds);
        pictureRepository.markPicturesShakyAsStable(albumId, pictureIds);
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
        if (pictureIds.contains(album.getThumbnailPicture().getId())){
            Optional<Picture> newThumbnailPicture = pictureRepository.findTopByAlbumAndDeletedAtIsNullOrderByQualityScoreDesc(album);
            newThumbnailPicture.ifPresent(album::setThumbnailPicture);
        }
    }

    @Transactional
    public void deleteAlbum(Long albumId) {
        User user = securityUtil.getCurrentUser();
        Album album = getAlbumIfMember(albumId);
        validAlbumOwner(album);
        LocalDateTime now = LocalDateTime.now();
        album.setDeletedAt(now);
        UserAlbum userAlbum = getUserAlbum(user, album);
        userAlbum.setDeletedAt(now);
        for (Picture p : album.getPictures()){
            p.setDeletedAt(now);
        }
        userAlbumRepository.save(userAlbum);
        pictureRepository.saveAll(album.getPictures());
        albumRepository.save(album);

    }

    private UserAlbum getUserAlbum(User user, Album album) {
        return userAlbumRepository.findByUserAndAlbum(user, album)
                .orElseThrow(()->new AlbumException(HttpStatus.NOT_FOUND, "앨범을 찾을 수 없습니다."));
    }

    private void checkAddPictureSize(int newSize, int previousSize) {
        if (newSize > MAX_PICTURE_SIZE) {
            int remaining = MAX_PICTURE_SIZE - previousSize;
            throw new AlbumException(
                    HttpStatus.BAD_REQUEST,
                    "같은 앨범에 사진은" + MAX_PICTURE_SIZE + "장을 초과하여 추가할 수 없습니다. 추가 가능한 사진 수: " + remaining + "장"
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

    private List<Picture> createPictures(
            List<? extends PictureUrlCoordinateDTO> pictureDTOs,
            Album album,
            User user
    ) {
        return pictureDTOs.stream()
                .map(dto -> Picture.builder()
                        .album(album)
                        .user(user)
                        .pictureURL(dto.pictureUrl())
                        .latitude(dto.latitude())
                        .longitude(dto.longitude())
                        .s3Key(presignedUrlService.extractS3Key(dto.pictureUrl()))
                        .build())
                .toList();
    }

//    private static List<Picture> createPictures(List<String> pictureUrls, Album album, User user) {
//        return pictureUrls.stream()
//                .map(url -> Picture.of(album, user, url))
//                .toList();
//    }

    private Album getEmptyAlbum(String albumName) {
        return Album.builder()
                .name(albumName)
                .userAlbums(new ArrayList<>())
                .pictures(new ArrayList<>())
                .build();
    }

    protected void validAlbumOwner(Album album) {
        UserAlbum userAlbum = getUserAlbum(securityUtil.getCurrentUser(), album);
        if (!userAlbum.getRole().equals(UserAlbumRole.OWNER)) {
            throw new AlbumException(HttpStatus.FORBIDDEN, "소유자가 아닙니다.");
        }
    }

    @Transactional
    public BaseApiResponse<String> createInviteToken(Long albumId){
        Album album = getAlbumIfMember(albumId);
        validAlbumOwner(album);

        String token = jwtTokenProvider.generateInviteToken(albumId);
        redisInviteTokenRepository.save(token, albumId);

        String prefix = isProd ? INVITE_LINK_PREFIX_PROD : INVITE_LINK_PREFIX_DEV;
        return BaseApiResponse.success("INVITE_LINK_CREATED", "초대 링크가 생성되었습니다.", prefix + token);
    }

    @Transactional
    public BaseApiResponse<AlbumInviteResponseDTO> acceptInvite(String token){
        if (redisInviteTokenRepository.existsByToken(token)) {
            Long tokenAlbumId = jwtTokenProvider.validateAndExtractInviteId(token);
            Album album = getAlbum(tokenAlbumId);
            User user = securityUtil.getCurrentUser();
            if (validateAlbumMember(album, user.getId())){
                throw new AlbumException(HttpStatus.BAD_REQUEST, "이미 초대된 구성원을 또다시 초대할 수 없습니다.");
            }
            UserAlbum userAlbum = UserAlbum.of(user, album, UserAlbumRole.NORMAL);
            userAlbumRepository.save(userAlbum);
            redisInviteTokenRepository.remove(token);
            AlbumInviteResponseDTO response = new AlbumInviteResponseDTO(tokenAlbumId,
                    album.getName());
            eventPublisher.publishEvent(new InviteMemberNotificationEvent(album.getId(), user.getId()));
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
        UserAlbum oldUserAlbum = getUserAlbum(oldOwner, getAlbumIfMember(albumId));
        UserAlbum newUserAlbum = getUserAlbum(newUser, getAlbumIfMember(albumId));
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

    @Transactional
    public BaseApiResponse<AlbumMemberResponseDTO> getAlbumMembers(Long albumId) {
        User user = securityUtil.getCurrentUser();
        Album album = getAlbumIfMember(albumId);
        List<UserAlbum> members = userAlbumRepository.findAllByAlbumAndUser(album, user);

        List<AlbumMemberResponseDTO.UserInfo> userInfos = members.stream()
                .map(ua -> {
                    User member = ua.getUser();
                    String rawUrl = member.getProfileImage();
                    String finalUrl = rawUrl;

                    if (rawUrl != null && rawUrl.contains("amazonaws.com")) {
                        if (member.getS3Key() == null) {
                            String key = presignedUrlService.extractS3Key(rawUrl);
                            member.setS3Key(key);
                            userRepository.save(member); // s3Key 저장
                        }
                        finalUrl = presignedUrlService.generateGetPresignedUrl(member.getS3Key());
                    }

                    return new AlbumMemberResponseDTO.UserInfo(
                            member.getId(),
                            member.getNickname(),
                            ua.getRole(),
                            finalUrl
                    );
                })
                .toList();

        return BaseApiResponse.success(
                "ALBUM_MEMBER_LIST_SUCCESS",
                "공동작업자 목록 조회 성공",
                new AlbumMemberResponseDTO(userInfos)
        );
    }

    @Transactional(readOnly = true)
    public BaseApiResponse<AlbumRoleResponseDTO> getAlbumRole(Long albumId) {
        User user = securityUtil.getCurrentUser();
        Album album = getAlbumIfMember(albumId);
        try{
            UserAlbumRole role = getUserAlbum(user, album).getRole();
            AlbumRoleResponseDTO responseDTO = new AlbumRoleResponseDTO(role);
            return BaseApiResponse.success(
                    "ALBUM_AUTHORIZED",
                    "앨범 접근 권한 있음",
                    responseDTO
            );
        } catch (Exception e){
            throw new AlbumException(HttpStatus.UNAUTHORIZED, "앨범 접근 권한이 없습니다.");
        }
    }
}
