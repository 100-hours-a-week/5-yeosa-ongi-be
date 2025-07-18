package ongi.ongibe.domain.album.service;


import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.album.UserAlbumRole;
import ongi.ongibe.cache.album.AlbumCacheService;
import ongi.ongibe.cache.user.UserCacheService;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.AlbumProcessState;
import ongi.ongibe.domain.album.dto.AlbumDetailResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumInviteResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumMemberResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumOwnerTransferResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumRoleResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumSummaryResponseDTO;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO;
import ongi.ongibe.domain.album.dto.PictureUrlCoordinateDTO;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.AlbumConcept;
import ongi.ongibe.domain.album.entity.FaceCluster;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.entity.PictureFaceCluster;
import ongi.ongibe.domain.album.event.AlbumEvent;
import ongi.ongibe.domain.album.exception.AlbumException;
import ongi.ongibe.domain.album.repository.AlbumConceptRepository;
import ongi.ongibe.domain.album.repository.CommentRepository;
import ongi.ongibe.domain.album.repository.FaceClusterRepository;
import ongi.ongibe.domain.album.repository.PictureFaceClusterRepository;
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
import ongi.ongibe.global.executor.TransactionAfterCommitExecutor;
import ongi.ongibe.global.s3.PresignedUrlService;
import ongi.ongibe.global.security.util.SecurityUtil;
import ongi.ongibe.global.util.DateUtil;
import ongi.ongibe.global.util.JwtTokenProvider;
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
    private final FaceClusterRepository faceClusterRepository;
    private final PictureFaceClusterRepository pictureFaceClusterRepository;
    private final PresignedUrlService presignedUrlService;
    private final AlbumCacheService albumCacheService;
    private final UserCacheService userCacheService;
    private final TransactionAfterCommitExecutor afterCommitExecutor;
    private final EntityManager entityManager;
    private final CommentRepository commentRepository;
    private final AlbumConceptRepository albumConceptRepository;

    @Value("${custom.isProd}")
    private boolean isProd;

    private static final String INVITE_LINK_PREFIX_PROD = "https://ongi.today/invite?token=";
    private static final String INVITE_LINK_PREFIX_DEV = "https://dev.ongi.today/invite?token=";
    private static final int MAX_PICTURE_SIZE = 30;
    private static final int MAX_ALBUM_MEMBER = 9;

    @Transactional(readOnly = true)
    public BaseApiResponse<MonthlyAlbumResponseDTO> getMonthlyAlbum(String yearMonth) {
        User user = securityUtil.getCurrentUser();
        MonthlyAlbumResponseDTO result = albumCacheService.getMonthlyAlbum(user.getId(), yearMonth);
        return BaseApiResponse.success("MONTHLY_ALBUM_SUCCESS", "앨범 조회 성공", result);
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

        List<FaceCluster> faceClusters = faceClusterRepository.findAllByRepresentativePicture_Album_Id(albumId);


        List<AlbumDetailResponseDTO.ClusterInfo> clusterInfos = faceClusters.stream()
                .map(cluster -> {
                    Picture representative = cluster.getRepresentativePicture();
                    List<PictureFaceCluster> mappings = pictureFaceClusterRepository.findAllByFaceCluster(cluster);
                    List<String> clusterPictureUrls = mappings.stream()
                            .map(PictureFaceCluster::getPicture)
                            .map(Picture::getPictureURL)
                            .toList();

                    return new AlbumDetailResponseDTO.ClusterInfo(
                            cluster.getId(),
                            cluster.getClusterName(),
                            representative.getPictureURL(),
                            cluster.getBboxX1(),
                            cluster.getBboxY1(),
                            cluster.getBboxX2(),
                            cluster.getBboxY2(),
                            clusterPictureUrls
                    );
                }).toList();

        int commentCount = commentRepository.countAllByAlbum(album);
        AlbumDetailResponseDTO responseDTO = new AlbumDetailResponseDTO(
                album.getName(),
                pictureInfos,
                album.getProcessState(),
                commentCount,
                clusterInfos
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
    public void createAlbum(String albumName, List<? extends PictureUrlCoordinateDTO> pictureDTOs, List<String> concepts) {
        if (pictureDTOs.size() > MAX_PICTURE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사진은 " + MAX_PICTURE_SIZE + "장을 초과하여 추가할 수 없습니다");
        }
        User user = securityUtil.getCurrentUser();
        Album album = getEmptyAlbum(albumName);
        List<Picture> pictures = createPictures(pictureDTOs, album, user);
        album.setPictures(pictures);
        album.setThumbnailPicture(pictures.getFirst());

        associateAlbumWithUser(user, album);
        persistAlbum(album, pictures);

        List<String> s3Keys = pictures.stream()
                .map(Picture::getS3Key)
                .toList();

        String yearMonth = DateUtil.getYearMonth(LocalDateTime.now());

        for (String concept : concepts) {
            AlbumConcept albumConcept = AlbumConcept.builder()
                    .album(album)
                    .concept(concept)
                    .build();
            albumConceptRepository.save(albumConcept);
        }

        afterCommitExecutor.execute(() ->{
            albumCacheService.refreshMonthlyAlbum(user.getId(), yearMonth);
            userCacheService.refreshUserTotalState(user);
            userCacheService.refreshUserTagState(user, yearMonth);
            userCacheService.refreshUserPictureStat(user, yearMonth);
            userCacheService.refreshUserPlaceStat(user, yearMonth);

            eventPublisher.publishEvent(new AlbumCreatedNotificationEvent(album.getId(), user.getId()));
            eventPublisher.publishEvent(new AlbumEvent(album.getId(), user.getId(), s3Keys, concepts));
        });
    }

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

        pictureRepository.saveAll(newPictures);
        albumRepository.save(album);

        List<String> pictureKeys = newPictures.stream()
                .map(Picture::getS3Key)
                .toList();

        List<String> concepts = albumConceptRepository.findAllByAlbum(album).stream()
                .map(AlbumConcept::getConcept)
                .toList();

        afterCommitExecutor.execute(() ->{
            refreshAllMemberMonthlyAlbumCache(album);
            refreshAllMemberTotalStateCache(album);

        });
        eventPublisher.publishEvent(new AlbumEvent(albumId, user.getId(), pictureKeys, concepts));
    }

    @Transactional
    public void updateAlbumName(Long albumId, String albumName) {
        Album album = getAlbumIfMember(albumId);
        validAlbumOwner(album);
        album.setName(albumName);
        albumRepository.save(album);

        refreshAllMemberMonthlyAlbumCache(album);
    }

    private void refreshAllMemberTotalStateCache(Album album) {
        List<User> members = userAlbumRepository.findAllByAlbum(album).stream()
                .map(UserAlbum::getUser).toList();

        String yearMonth = DateUtil.getYearMonth(album.getCreatedAt());
        for (User user : members) {
            userCacheService.refreshUserTotalState(user);
            userCacheService.refreshUserTagState(user, yearMonth);
            userCacheService.refreshUserPictureStat(user, yearMonth);
            userCacheService.refreshUserPlaceStat(user, yearMonth);
        }
    }

    private void refreshAllMemberMonthlyAlbumCache(Album album) {
        String yearMonth = DateUtil.getYearMonth(album.getCreatedAt());
        List<Long> memberIds = userAlbumRepository.findAllByAlbum(album).stream()
                .map(UserAlbum::getUser)
                .map(User::getId).toList();

        for (Long userId : memberIds) {
            albumCacheService.refreshMonthlyAlbum(userId, yearMonth);
        }
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

        List<Long> representativeIds = faceClusterRepository.findAllRepresentativePictureIds();
        if (pictureIds.stream().anyMatch(representativeIds::contains)) {
            throw new AlbumException(HttpStatus.BAD_REQUEST, "대표 사진은 삭제할 수 없습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        for (Picture p : pictures) {
            p.setDeletedAt(now);
        }

        if (pictureIds.contains(album.getThumbnailPicture().getId())){
            Optional<Picture> newThumbnailPicture = pictureRepository.findTopByAlbumAndDeletedAtIsNullOrderByQualityScoreDesc(album);
            newThumbnailPicture.ifPresent(album::setThumbnailPicture);
        }
        pictureFaceClusterRepository.deleteAllByPictureIds(now, pictureIds);

        afterCommitExecutor.execute(() ->{
            refreshAllMemberTotalStateCache(album);
            refreshAllMemberMonthlyAlbumCache(album);
        });
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

        entityManager.flush();

        albumCacheService.evictMonthlyAlbum(user.getId(), DateUtil.getYearMonth(album.getCreatedAt()));
        afterCommitExecutor.execute(() ->{
            refreshAllMemberTotalStateCache(album);
            refreshAllMemberMonthlyAlbumCache(album);
        });
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
                        .createdAt(LocalDateTime.now())
                        .createdDate(LocalDate.now())
                        .build())
                .toList();
    }

    private Album getEmptyAlbum(String albumName) {
        return Album.builder()
                .name(albumName)
                .userAlbums(new ArrayList<>())
                .pictures(new ArrayList<>())
                .processState(AlbumProcessState.NOT_STARTED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    protected void validAlbumOwner(Album album) {
        UserAlbum userAlbum = getUserAlbum(securityUtil.getCurrentUser(), album);
        if (!userAlbum.getRole().equals(UserAlbumRole.OWNER)) {
            throw new AlbumException(HttpStatus.FORBIDDEN, "소유자가 아닙니다.");
        }
    }

    @Transactional
    public void updateClusterName(Long albumId, Long clusterId, String newClusterName) {
        Album album = getAlbumIfMember(albumId);
        FaceCluster faceCluster = faceClusterRepository.findById(clusterId).orElseThrow(
                () -> new AlbumException(HttpStatus.NOT_FOUND, "해당 클러스터를 찾을 수 없습니다.")
        );
        faceCluster.setClusterName(newClusterName);
        faceClusterRepository.save(faceCluster);
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
            if (userAlbumRepository.findAllByAlbum(album).size() > MAX_ALBUM_MEMBER) {
                throw new AlbumException(HttpStatus.BAD_REQUEST, "앨범 구성원 정원 초과입니다.");
            }
            UserAlbum userAlbum = UserAlbum.of(user, album, UserAlbumRole.NORMAL);
            userAlbumRepository.save(userAlbum);
            AlbumInviteResponseDTO response = new AlbumInviteResponseDTO(tokenAlbumId,
                    album.getName());

            afterCommitExecutor.execute(()->{
                refreshAllMemberTotalStateCache(album);
                refreshAllMemberMonthlyAlbumCache(album);
            });
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

    @Transactional(readOnly = true)
    public BaseApiResponse<AlbumMemberResponseDTO> getAlbumMembers(Long albumId) {
        User user = securityUtil.getCurrentUser();
        Album album = getAlbumIfMember(albumId);
        List<UserAlbum> members = userAlbumRepository.findAllByAlbum(album);

        List<AlbumMemberResponseDTO.UserInfo> userInfos = members.stream()
                .map(ua -> {
                    User member = ua.getUser();

                    return new AlbumMemberResponseDTO.UserInfo(
                            member.getId(),
                            member.getNickname(),
                            ua.getRole(),
                            member.getProfileImage()
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
        try{
            Album album = getAlbumIfMember(albumId);
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
