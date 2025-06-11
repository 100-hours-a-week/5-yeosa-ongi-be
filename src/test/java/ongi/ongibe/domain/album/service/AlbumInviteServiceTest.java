package ongi.ongibe.domain.album.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ongi.ongibe.UserAlbumRole;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.AlbumInviteResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumMemberResponseDTO;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.UserAlbum;
import ongi.ongibe.domain.album.exception.AlbumException;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.RedisInviteTokenRepository;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.domain.auth.OAuthProvider;
import ongi.ongibe.domain.user.UserStatus;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.global.security.util.SecurityUtil;
import ongi.ongibe.util.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AlbumInviteServiceTest {

    @InjectMocks
    private AlbumService albumService;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RedisInviteTokenRepository redisInviteTokenRepository;

    @Mock
    private UserAlbumRepository userAlbumRepository;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private final Long albumId = 1L;
    private final String token = "mocked.jwt.token";

    private Album testAlbum;
    private User testUser;
    private User inviteTestUser;
    private UserAlbum testUserAlbum;

    @BeforeEach
    void setUp() {
        testAlbum = Album.builder()
                .id(albumId)
                .name("여행 앨범")
                .pictures(new ArrayList<>())
                .userAlbums(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        testUser = User.builder()
                .id(123L)
                .nickname("owner-user")
                .providerId("own123")
                .provider(OAuthProvider.KAKAO)
                .userStatus(UserStatus.ACTIVE)
                .profileImage("owner.png")
                .userAlbums(new ArrayList<>())
                .build();

        testUserAlbum = UserAlbum.of(testUser, testAlbum, UserAlbumRole.OWNER);

        inviteTestUser = User.builder()
                .id(124L)
                .nickname("invite-user")
                .providerId("inv124")
                .provider(OAuthProvider.KAKAO)
                .userStatus(UserStatus.ACTIVE)
                .profileImage("invite.png")
                .userAlbums(new ArrayList<>())
                .build();

        User member1 = User.builder()
                .id(125L)
                .nickname("member1")
                .providerId("mem125")
                .provider(OAuthProvider.KAKAO)
                .userStatus(UserStatus.ACTIVE)
                .profileImage("m1.png")
                .userAlbums(new ArrayList<>())
                .build();

        User member2 = User.builder()
                .id(126L)
                .nickname("member2")
                .providerId("mem126")
                .provider(OAuthProvider.KAKAO)
                .userStatus(UserStatus.ACTIVE)
                .profileImage("m2.png")
                .userAlbums(new ArrayList<>())
                .build();

        UserAlbum member1UA = UserAlbum.of(member1, testAlbum, UserAlbumRole.NORMAL);
        UserAlbum member2UA = UserAlbum.of(member2, testAlbum, UserAlbumRole.NORMAL);

        testAlbum.getUserAlbums().addAll(List.of(testUserAlbum, member1UA, member2UA));
        testUser.getUserAlbums().add(testUserAlbum);
        member1.getUserAlbums().add(member1UA);
        member2.getUserAlbums().add(member2UA);
    }

    @Test
    void createInviteToken_성공() {
        // given
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(testAlbum));
        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        when(userAlbumRepository.findByUserAndAlbum(testUser, testAlbum)).thenReturn(Optional.of(testUserAlbum));
        when(jwtTokenProvider.generateInviteToken(albumId)).thenReturn(token);

        // when
        BaseApiResponse<String> result = albumService.createInviteToken(albumId);

        // then
        assertThat(result.getData()).isEqualTo("https://dev.ongi.today/invite?token=" + token);
        verify(redisInviteTokenRepository).save(token, albumId);
    }

    @Test
    void acceptInvite_성공() {
        // given
        when(redisInviteTokenRepository.existsByToken(token)).thenReturn(true);
        when(jwtTokenProvider.validateAndExtractInviteId(token)).thenReturn(albumId);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(testAlbum));
        when(securityUtil.getCurrentUser()).thenReturn(inviteTestUser);

        // when
        BaseApiResponse<AlbumInviteResponseDTO> result = albumService.acceptInvite(token);

        // then
        assertThat(result.getData().albumId()).isEqualTo(albumId);
        assertThat(result.getData().albumName()).isEqualTo("여행 앨범");
        verify(userAlbumRepository).save(any(UserAlbum.class));

        ArgumentCaptor<UserAlbum> captor = ArgumentCaptor.forClass(UserAlbum.class);
        verify(userAlbumRepository).save(captor.capture());
        UserAlbum saved = captor.getValue();

        assertThat(saved.getUser()).isEqualTo(inviteTestUser);
        assertThat(saved.getAlbum()).isEqualTo(testAlbum);
        assertThat(saved.getRole()).isEqualTo(UserAlbumRole.NORMAL);
        verify(redisInviteTokenRepository).remove(token);
    }

    @Test
    void acceptInvite_토큰없음_예외() {
        // given
        when(redisInviteTokenRepository.existsByToken(token)).thenReturn(false);

        // when, then
        assertThatThrownBy(() -> albumService.acceptInvite(token))
                .isInstanceOf(AlbumException.class)
                .hasMessageContaining("초대코드");
    }

    @Test
    void acceptInvite_이미_구성원임(){
        //given
        when(redisInviteTokenRepository.existsByToken(token)).thenReturn(true);
        when(jwtTokenProvider.validateAndExtractInviteId(token)).thenReturn(albumId);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(testAlbum));
        when(securityUtil.getCurrentUser()).thenReturn(testUser);

        UserAlbum userAlbum = UserAlbum.of(testUser, testAlbum, UserAlbumRole.NORMAL);
        testAlbum.getUserAlbums().add(userAlbum);

        //when, then
        assertThatThrownBy(() -> albumService.acceptInvite(token))
                .isInstanceOf(AlbumException.class)
                .hasMessageContaining("이미 초대된");
    }

    @Test
    void getAlbumMember_정상조회() {
        // given
        when(securityUtil.getCurrentUser()).thenReturn(testUser); // OWNER
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(testAlbum));
        when(userAlbumRepository.findAllByAlbum(testAlbum))
                .thenReturn(testAlbum.getUserAlbums());

        // when
        BaseApiResponse<AlbumMemberResponseDTO> result = albumService.getAlbumMembers(albumId);

        // then
        assertThat(result.getCode()).isEqualTo("ALBUM_MEMBER_LIST_SUCCESS");
        assertThat(result.getMessage()).isEqualTo("공동작업자 목록 조회 성공");
        assertThat(result.getData().userInfo().size()).isEqualTo(3);

        boolean containsOwner = result.getData().userInfo().stream()
                .anyMatch(userInfo ->
                        userInfo.userId().equals(testUser.getId())
                                && userInfo.role() == UserAlbumRole.OWNER
                                && userInfo.nickname().equals("owner-user")
                );

        boolean containsNormal1 = result.getData().userInfo().stream()
                .anyMatch(userInfo ->
                        userInfo.nickname().equals("member1")
                                && userInfo.role() == UserAlbumRole.NORMAL
                );

        boolean containsNormal2 = result.getData().userInfo().stream()
                .anyMatch(userInfo ->
                        userInfo.nickname().equals("member2")
                                && userInfo.role() == UserAlbumRole.NORMAL
                );

        assertThat(containsOwner).isTrue();
        assertThat(containsNormal1).isTrue();
        assertThat(containsNormal2).isTrue();
    }
}
