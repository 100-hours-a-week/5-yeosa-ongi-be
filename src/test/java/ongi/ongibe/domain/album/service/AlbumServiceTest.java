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

import ongi.ongibe.UserAlbumRole;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.AlbumProcessState;
import ongi.ongibe.domain.album.dto.AlbumCreateRequestDTO;
import ongi.ongibe.domain.album.dto.AlbumCreateRequestGeoFrontDTO;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


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
        System.out.println("Mocked currentUserId = " + testUser.getId());

        when(securityUtil.getCurrentUserId()).thenReturn(testUser.getId());
        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        doNothing().when(eventPublisher).publishEvent(any());

        for (int i = 1; i <= 5; i++) {
            // 1. 앨범마다 사용할 3개 지역 랜덤 추출
            List<String[]> shuffledLocations = new ArrayList<>(SAMPLE_LOCATIONS);
            Collections.shuffle(shuffledLocations);
            List<Place> albumPlaces = SAMPLE_LOCATIONS.subList(0, 3).stream()
                    .map(loc -> Place.builder()
                            .city(loc[0])
                            .district(loc[1])
                            .town(loc[2])
                            .build())
                    .map(placeRepository::save)
                    .toList();

            List<Picture> pictures = new ArrayList<>();
            for (int j = 1; j <= 10; j++) {
                Place place = albumPlaces.get(j % 3); // 순환적으로 3개 중 하나씩 부여
                Picture picture = Picture.builder()
                        .user(testUser)
                        .pictureURL("https://cdn.ongi.today/pic-" + i + "-" + j + ".jpg")
                        .place(place)
                        .tag(tags.get((j - 1) % tags.size()))       // 개, 고양이, 사람, 술 순환
                        .isDuplicated(j % 2 == 0)                   // true/false 번갈아
                        .isShaky(j % 2 != 0)                        // true/false 번갈아
                        .qualityScore((float) (50 + Math.random() * 50)) // 50~100 사이 랜덤 float
                        .createdAt(LocalDateTime.of(2025, 7 - i, 1, 0, 0))
                        .createdDate(LocalDate.of(2025, 7 - i, 1))
                        .build();
                pictures.add(picture);
            }

            Picture thumbnail = pictures.getFirst();
            Album album = Album.builder()
                    .name("앨범 " + i)
                    .processState(AlbumProcessState.NOT_STARTED)
                    .thumbnailPicture(thumbnail)
                    .pictures(pictures)
                    .build();
            album.setCreatedAt(LocalDateTime.of(2025, 7 - i, 1, 0, 0));
            for (Picture picture : pictures) {
                picture.setAlbum(album);
            }

            album = albumRepository.saveAndFlush(album);
            pictureRepository.saveAll(pictures);

            // 3개의 사진 클러스터 구성
            Picture representative = pictures.getFirst(); // 대표 사진
            FaceCluster faceCluster = FaceCluster.builder()
                    .representativePicture(representative)
                    .clusterName("사람-" + i) // 예: 사람-1, 사람-2 ...
                    .bboxX1(100).bboxY1(100).bboxX2(300).bboxY2(300)
                    .build();
            faceCluster = faceClusterRepository.save(faceCluster);

            // 클러스터에 속한 사진 지정 (앞 3장)
            List<PictureFaceCluster> clusterLinks = List.of(
                    PictureFaceCluster.builder().picture(pictures.get(0)).faceCluster(faceCluster).build(),
                    PictureFaceCluster.builder().picture(pictures.get(1)).faceCluster(faceCluster).build(),
                    PictureFaceCluster.builder().picture(pictures.get(2)).faceCluster(faceCluster).build()
            );
            pictureFaceClusterRepository.saveAll(clusterLinks);

            UserAlbum userAlbum = UserAlbum.of(testUser, album, UserAlbumRole.OWNER);
            album.setUserAlbums(List.of(userAlbum));
            userAlbumRepository.save(userAlbum);
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

        albumService.createAlbum(newAlbumName, pictureDTOs);

        assertThat(albumRepository.findByName("newAlbumName")).isNotNull();
        assertThat(albumRepository.findByName("newAlbumName").orElseThrow().getPictures().size()).isEqualTo(2);
    }
}
