package ongi.ongibe.domain.album.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import ongi.ongibe.domain.album.UserAlbumRole;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.AlbumProcessState;
import ongi.ongibe.domain.album.dto.AlbumCreateRequestGeoFrontDTO.PictureRequestDTO;
import ongi.ongibe.domain.album.dto.AlbumDetailResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumSummaryResponseDTO;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO;
import ongi.ongibe.domain.album.dto.PictureUrlCoordinateDTO;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.FaceCluster;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.entity.PictureFaceCluster;
import ongi.ongibe.domain.album.entity.UserAlbum;
import ongi.ongibe.domain.album.exception.AlbumException;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.FaceClusterRepository;
import ongi.ongibe.domain.album.repository.PictureFaceClusterRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.album.repository.PlaceRepository;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.domain.auth.OAuthProvider;
import ongi.ongibe.domain.place.entity.Place;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.security.util.SecurityUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AlbumServiceTest {

    @Autowired private AlbumService albumService;
    @Autowired private AlbumRepository albumRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserAlbumRepository userAlbumRepository;
    @Autowired private PictureRepository pictureRepository;
    @Autowired private PlaceRepository placeRepository;
    @Autowired private FaceClusterRepository faceClusterRepository;
    @Autowired private PictureFaceClusterRepository pictureFaceClusterRepository;

    @MockitoBean
    private ApplicationEventPublisher eventPublisher;
    @MockitoBean
    private SecurityUtil securityUtil;

    private static final List<String[]> SAMPLE_LOCATIONS = List.of(
            new String[]{"서울", "강남구", "역삼동"},
            new String[]{"부산", "해운대구", "우동"},
            new String[]{"대구", "중구", "동인동"},
            new String[]{"인천", "연수구", "송도동"},
            new String[]{"광주", "북구", "중흥동"},
            new String[]{"대전", "서구", "둔산동"},
            new String[]{"울산", "남구", "삼산동"},
            new String[]{"세종", "세종시", "어진동"},
            new String[]{"경기", "성남시", "분당동"},
            new String[]{"강원", "춘천시", "석사동"}
    );

    private static final List<String> tags = List.of("개", "고양이", "사람", "술");

    User testUser = User.builder()
            .nickname("testUser")
            .email("test@example.com")
            .profileImage("default.png")
            .provider(OAuthProvider.KAKAO)
            .providerId("providerId")
            .build();

    User invalidTestUser = User.builder()
            .nickname("invalidTestUser")
            .email("invalidTestUser@example.com")
            .profileImage("default.png")
            .provider(OAuthProvider.KAKAO)
            .providerId("providerId")
            .build();


    @BeforeEach
    void setUp() {
        userRepository.save(invalidTestUser);
        testUser = userRepository.save(testUser);
        when(securityUtil.getCurrentUserId()).thenReturn(testUser.getId());
        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        doNothing().when(eventPublisher).publishEvent(any());

        for (int i = 1; i <= 5; i++) {

            /* 1. 앨범마다 3개 지역 랜덤 지정 */
            List<String[]> shuffledLocations = new ArrayList<>(SAMPLE_LOCATIONS);
            Collections.shuffle(shuffledLocations);
            List<Place> albumPlaces = shuffledLocations.subList(0, 3).stream()    // ✅ 여기!
                    .map(loc -> Place.builder()
                            .city(loc[0]).district(loc[1]).town(loc[2]).build())
                    .map(placeRepository::save)
                    .toList();

            /* 2. Picture 10장 */
            List<Picture> pictures = new ArrayList<>();
            for (int j = 1; j <= 10; j++) {
                Place place = albumPlaces.get(j % 3);
                pictures.add(Picture.builder()
                        .user(testUser)
                        .pictureURL("https://cdn.ongi.today/pic-" + i + "-" + j + ".jpg")
                        .place(place)
                        .tag(tags.get((j - 1) % tags.size()))
                        .isDuplicated(j % 2 == 0)
                        .isShaky(j % 2 != 0)
                        .qualityScore((float) (50 + Math.random() * 50))
                        .createdAt(LocalDateTime.of(2025, 7 - i, 1, 0, 0))
                        .createdDate(LocalDate.of(2025, 7 - i, 1))
                        .build());
            }

            /* 3. Album 생성 */
            Picture thumbnail = pictures.get(2);
            Album album = Album.builder()
                    .name("앨범 " + i)
                    .processState(AlbumProcessState.NOT_STARTED)
                    .thumbnailPicture(thumbnail)
                    .pictures(pictures)               // 이미 mutable ArrayList
                    .build();
            album.setCreatedAt(LocalDateTime.of(2025, 7 - i, 1, 0, 0));
            pictures.forEach(p -> p.setAlbum(album)); // 역방향 연관

            /* 4. UserAlbum(OWNER) 추가 – 리스트 '교체' 아니라 '추가' */
            UserAlbum ownerUA = UserAlbum.of(testUser, album, UserAlbumRole.OWNER);
            album.getUserAlbums().add(ownerUA);        // add 로 유지
            testUser.getUserAlbums().add(ownerUA);     // (양방향)

            /* 5. 저장 – cascade 로 userAlbums, pictures 모두 저장 */
            albumRepository.saveAndFlush(album);

            /* 6. 얼굴 클러스터 예시 */
            FaceCluster faceCluster = faceClusterRepository.save(FaceCluster.builder()
                    .representativePicture(pictures.getFirst())
                    .clusterName("사람-" + i)
                    .bboxX1(100).bboxY1(100).bboxX2(300).bboxY2(300)
                    .build());

            pictureFaceClusterRepository.saveAll(List.of(
                    PictureFaceCluster.builder().picture(pictures.get(0)).faceCluster(faceCluster).deletedAt(null).build(),
                    PictureFaceCluster.builder().picture(pictures.get(1)).faceCluster(faceCluster).deletedAt(null).build(),
                    PictureFaceCluster.builder().picture(pictures.get(2)).faceCluster(faceCluster).deletedAt(null).build()
            ));
        }
    }

    @Test
    void getMonthlyAlbum_정상조회_마지막월있음() {
        // when
        BaseApiResponse<MonthlyAlbumResponseDTO> response = albumService.getMonthlyAlbum("2025-06");

        // then
        assertThat(response.getData().albumInfo()).hasSize(1);
        assertThat(response.getData().albumInfo().getFirst().albumName()).isEqualTo("앨범 1");
    }

    @Test
    void getMonthlyAlbum_정상조회_마지막월없음() {
        // when
        BaseApiResponse<MonthlyAlbumResponseDTO> response = albumService.getMonthlyAlbum("2025-02");

        // then
        assertThat(response.getData().albumInfo()).hasSize(1);
        assertThat(response.getData().albumInfo().getFirst().albumName()).isEqualTo("앨범 5");
    }

    @Test
    void getMonthlyAlbum_새로운유저_접근_빈값반환() {
        //given
        when(securityUtil.getCurrentUserId()).thenReturn(invalidTestUser.getId());
        when(securityUtil.getCurrentUser()).thenReturn(invalidTestUser);

        //when
        BaseApiResponse<MonthlyAlbumResponseDTO> response = albumService.getMonthlyAlbum("2025-06");

        //then
        assertThat(response.getData().albumInfo()).hasSize(0);
    }

    @Test
    void getAlbumSummary_정상조회() {
        //given
        Long albumId = albumRepository.findAll().stream()
                .filter(album -> album.getName().equals("앨범 1"))
                .findFirst()
                .orElseThrow()
                .getId();
        //when
        BaseApiResponse<List<AlbumSummaryResponseDTO>> response = albumService.getAlbumSummary(albumId);

        //then
        assertThat(response.getData().size()).isEqualTo(3);
    }

    @Test
    void getAlbumDetail_정상조회() {
        //given
        Long albumId = albumRepository.findAll().stream()
                .filter(album -> album.getName().equals("앨범 1"))
                .findFirst()
                .orElseThrow()
                .getId();

        //when
        BaseApiResponse<AlbumDetailResponseDTO> response = albumService.getAlbumDetail(albumId);

        //then
        AlbumDetailResponseDTO detail = response.getData();
        assertThat(detail).isNotNull();
        assertThat(detail.title()).isEqualTo("앨범 1");
        assertThat(detail.cluster().size()).isEqualTo(1);
        assertThat(detail.picture().size()).isEqualTo(10);
    }

    @Test
    void getAlbumDetail_앨범멤버아님() {
        //given
        Long albumId = albumRepository.findAll().stream()
                .filter(album -> album.getName().equals("앨범 1"))
                .findFirst()
                .orElseThrow()
                .getId();
        when(securityUtil.getCurrentUserId()).thenReturn(invalidTestUser.getId());
        when(securityUtil.getCurrentUser()).thenReturn(invalidTestUser);

        //when then
        assertThatThrownBy(() -> albumService.getAlbumDetail(albumId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("앨범 멤버가 아닙니다.");
    }

    @Test
    void createAlbum_정상작동() {
        String newAlbumName = "newAlbumName";
        List<? extends PictureUrlCoordinateDTO> pictureDTOs = List.of(
                new PictureRequestDTO("link1.jpeg", 37.5665, 126.9780),
                new PictureRequestDTO("link2.jpeg", 35.5665, 126.9780)
        );
        List<String> concepts = List.of("test1", "test2", "test3");

        albumService.createAlbum(newAlbumName, pictureDTOs, concepts);

        assertThat(albumRepository.findByName("newAlbumName")).isNotNull();
        assertThat(albumRepository.findByName("newAlbumName").orElseThrow().getPictures().size()).isEqualTo(2);
    }

    @Test
    void createAlbum_한도초과() {
        String newAlbumName = "newAlbumName";
        List<? extends PictureUrlCoordinateDTO> pictureDTOs = IntStream.range(1, 36)
                .mapToObj(i -> new PictureRequestDTO(
                        "link" + i + ".jpeg",
                        37.0 + (i * 0.01),  // 위도: 37.01 ~ 37.35
                        126.0 + (i * 0.01)  // 경도: 126.01 ~ 126.35
                ))
                .toList();
        List<String> concepts = List.of("test1", "test2", "test3");

        assertThatThrownBy(() -> albumService.createAlbum(newAlbumName, pictureDTOs, concepts))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("장을 초과하여 추가할 수 없습니다");
    }

    @Test
    void addPicture_정상추가() {
        //givne
        List<? extends PictureUrlCoordinateDTO> pictureDTOs = IntStream.range(1, 11)
                .mapToObj(i -> new PictureRequestDTO(
                        "link" + i + ".jpeg",
                        37.0 + (i * 0.01),
                        126.0 + (i * 0.01)
                ))
                .collect(Collectors.toCollection(ArrayList::new));
        Long albumId = albumRepository.findAll().stream()
                .filter(album -> album.getName().equals("앨범 1"))
                .findFirst()
                .orElseThrow()
                .getId();
        when(securityUtil.getCurrentUserId()).thenReturn(testUser.getId());

        //when
        albumService.addPictures(albumId, pictureDTOs);

        //then
        Album updatedAlbum = albumRepository.findById(albumId).orElseThrow();
        List<Picture> pictures = pictureRepository.findAllByAlbum(updatedAlbum);
        assertThat(pictures).hasSize(20);
    }

    @Test
    void addPicture_사진개수초과(){
        //givne
        List<? extends PictureUrlCoordinateDTO> pictureDTOs = IntStream.range(1, 31)
                .mapToObj(i -> new PictureRequestDTO(
                        "link" + i + ".jpeg",
                        37.0 + (i * 0.01),
                        126.0 + (i * 0.01)
                ))
                .collect(Collectors.toCollection(ArrayList::new));
        Long albumId = albumRepository.findAll().stream()
                .filter(album -> album.getName().equals("앨범 1"))
                .findFirst()
                .orElseThrow()
                .getId();
        when(securityUtil.getCurrentUserId()).thenReturn(testUser.getId());

        //when then
        assertThatThrownBy(() -> albumService.addPictures(albumId, pictureDTOs))
                .isInstanceOf(AlbumException.class)
                .hasMessageContaining("장을 초과하여 추가할 수 없습니다. 추가 가능한 사진 수: ");
    }

    @Test
    void updateAlbumName_정상동작() {
        //given
        Long albumId = albumRepository.findAll().stream()
                .filter(album -> album.getName().equals("앨범 1"))
                .findFirst()
                .orElseThrow()
                .getId();
        when(securityUtil.getCurrentUser()).thenReturn(testUser);

        //when
        albumService.updateAlbumName(albumId, "앨범 111");

        //then
        assertThat(albumRepository.findById(albumId).orElseThrow().getName()).isEqualTo("앨범 111");
    }

    @Test
    void updatePicture_정상동작() {
        //given
        Long albumId = albumRepository.findAll().stream()
                .filter(album -> album.getName().equals("앨범 1"))
                .findFirst()
                .orElseThrow()
                .getId();
        when(securityUtil.getCurrentUserId()).thenReturn(testUser.getId());
        List<Long> pictureIds = pictureRepository.findAllByAlbumId(albumId).stream()
                .filter(Picture::isDuplicated)
                .map(Picture::getId)
                .toList();

        //when
        albumService.updatePicture(albumId, pictureIds);

        //then
        assertThat(pictureRepository.findAllByAlbumId(albumId).stream()
                .filter(Picture::isDuplicated)
                .toList()).hasSize(0);
    }

    @Test
    void deletePicture_정상동작_썸네일삭제(){
        //given
        Long albumId = albumRepository.findAll().stream()
                .filter(album -> album.getName().equals("앨범 1"))
                .findFirst()
                .orElseThrow()
                .getId();
        when(securityUtil.getCurrentUserId()).thenReturn(testUser.getId());
        List<Long> pictureIds = pictureRepository.findAllByAlbumId(albumId).stream()
                .map(Picture::getId)
                .skip(1)
                .limit(1)
                .toList();

        //when
        albumService.deletePictures(albumId, pictureIds);

        //then
        assertThat(pictureRepository.findAllByAlbumId(albumId).stream()
                .toList()).hasSize(9);
        assertThat(albumRepository.findById(albumId).orElseThrow().getThumbnailPicture()).isNotNull();
    }

    @Test
    void deletePicture_정상동작_일반사진삭제(){
        //given
        Long albumId = albumRepository.findAll().stream()
                .filter(album -> album.getName().equals("앨범 1"))
                .findFirst()
                .orElseThrow()
                .getId();
        when(securityUtil.getCurrentUserId()).thenReturn(testUser.getId());
        List<Long> pictureIdnotSort = pictureRepository.findAllByAlbumId(albumId).stream()
                .map(Picture::getId)
                .toList();
        List<Long> pictureIds = pictureIdnotSort.subList(3, 5);

        //when
        albumService.deletePictures(albumId, pictureIds);

        //then
        assertThat(pictureRepository.findAllByAlbumId(albumId).stream()
                .toList()).hasSize(8);
    }

    @Test
    void deletePicture_삭제할수없는사진(){
        //given
        Long albumId = albumRepository.findAll().stream()
                .filter(album -> album.getName().equals("앨범 1"))
                .findFirst()
                .orElseThrow()
                .getId();

        Long album2Id = albumRepository.findAll().stream()
                .filter(album -> album.getName().equals("앨범 2"))
                .findFirst()
                .orElseThrow()
                .getId();
        when(securityUtil.getCurrentUserId()).thenReturn(testUser.getId());
        List<Long> pictureIds = pictureRepository.findAllByAlbumId(album2Id).stream()
                .map(Picture::getId)
                .toList();

        //when then
        assertThatThrownBy(() -> albumService.deletePictures(albumId, pictureIds))
                .isInstanceOf(AlbumException.class)
                .hasMessageContaining("삭제할 수 없는 사진이 포함되어 있습니다.");
    }

    @Test
    void deletePicture_representativePicture_삭제시도() {
        //givne
        Long albumId = albumRepository.findAll().stream()
                .filter(album -> album.getName().equals("앨범 1"))
                .findFirst()
                .orElseThrow()
                .getId();

        when(securityUtil.getCurrentUserId()).thenReturn(testUser.getId());
        List<Long> pictureIds = pictureRepository.findAllByAlbumId(albumId).stream()
                .map(Picture::getId)
                .limit(1)
                .toList();

        //when then
        assertThatThrownBy(()->albumService.deletePictures(albumId, pictureIds))
                .isInstanceOf(AlbumException.class)
                .hasMessageContaining("대표 사진은 삭제할 수 없습니다.");
    }

    @Test
    void deleteAlbum_정상동작() {
        //given
        Long albumId = albumRepository.findAll().stream()
                .filter(album -> album.getName().equals("앨범 1"))
                .findFirst()
                .orElseThrow()
                .getId();
        when(securityUtil.getCurrentUserId()).thenReturn(testUser.getId());

        //when
        albumService.deleteAlbum(albumId);

        //then
        Album deletedAlbum = albumRepository.findById(albumId).orElseThrow();
        assertThat(deletedAlbum.getDeletedAt()).isNotNull();
    }

    @Test
    void updateCluster_정상동작() {
        //given
        Long albumId = albumRepository.findAll().stream()
                .filter(album -> album.getName().equals("앨범 1"))
                .findFirst()
                .orElseThrow()
                .getId();
        when(securityUtil.getCurrentUserId()).thenReturn(testUser.getId());

        Long clusterId = faceClusterRepository.findAll().stream()
                .filter(faceCluster -> faceCluster.getRepresentativePicture().getAlbum().getId().equals(albumId))
                .findFirst()
                .orElseThrow()
                .getId();
        String newName = "변경된이름";

        // when
        albumService.updateClusterName(albumId, clusterId, newName);

        //then
        FaceCluster cluster = faceClusterRepository.findById(clusterId).orElseThrow();
        assertThat(cluster.getClusterName()).isEqualTo(newName);
    }

    @Test
    void updateCluster_clusterId_못찾음(){
        //given
        Long albumId = albumRepository.findAll().stream()
                .filter(album -> album.getName().equals("앨범 1"))
                .findFirst()
                .orElseThrow()
                .getId();
        when(securityUtil.getCurrentUserId()).thenReturn(testUser.getId());

        Long invalidClusterId = faceClusterRepository.findAll().stream()
                .mapToLong(FaceCluster::getId)
                .max()
                .orElse(0L) + 1;

        // when then
        assertThatThrownBy(() -> albumService.updateClusterName(albumId, invalidClusterId, "new"))
                .isInstanceOf(AlbumException.class)
                .hasMessageContaining("해당 클러스터를 찾을 수 없습니다");

    }
}
