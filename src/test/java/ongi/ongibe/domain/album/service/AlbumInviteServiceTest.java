package ongi.ongibe.domain.album.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ongi.ongibe.domain.album.UserAlbumRole;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.AlbumInviteResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumMemberResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumOwnerTransferResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumRoleResponseDTO;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.UserAlbum;
import ongi.ongibe.domain.album.exception.AlbumException;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.RedisInviteTokenRepository;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.domain.auth.OAuthProvider;
import ongi.ongibe.domain.notification.event.InviteMemberNotificationEvent;
import ongi.ongibe.domain.user.UserStatus;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.executor.TransactionAfterCommitExecutor;
import ongi.ongibe.global.security.util.SecurityUtil;
import ongi.ongibe.global.util.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private TransactionAfterCommitExecutor transactionAfterCommitExecutor;

    private final Long albumId = 1L;
    private final String token = "mocked.jwt.token";

    private Album testAlbum;
    private User testUser;
    private User member1;
    private User member2;
    private User inviteTestUser;
    private UserAlbum testUserAlbum;
    private UserAlbum member1UA;
    private UserAlbum member2UA;

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

        member1 = User.builder()
                .id(125L)
                .nickname("member1")
                .providerId("mem125")
                .provider(OAuthProvider.KAKAO)
                .userStatus(UserStatus.ACTIVE)
                .profileImage("m1.png")
                .userAlbums(new ArrayList<>())
                .build();

        member2 = User.builder()
                .id(126L)
                .nickname("member2")
                .providerId("mem126")
                .provider(OAuthProvider.KAKAO)
                .userStatus(UserStatus.ACTIVE)
                .profileImage("m2.png")
                .userAlbums(new ArrayList<>())
                .build();

        member1UA = UserAlbum.of(member1, testAlbum, UserAlbumRole.NORMAL);
        member2UA = UserAlbum.of(member2, testAlbum, UserAlbumRole.NORMAL);

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
    void createInviteToken_앨범소유자아님() {
        // given
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(testAlbum));
        when(securityUtil.getCurrentUser()).thenReturn(member1);
        when(userAlbumRepository.findByUserAndAlbum(member1, testAlbum)).thenReturn(Optional.of(member1UA));

        //when then
        assertThatThrownBy(() -> albumService.createInviteToken(albumId))
                .isInstanceOf(AlbumException.class)
                .hasMessageContaining("소유자가 아닙니다.");
    }

    @Test
    void acceptInvite_성공_한명() {
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
        verify(transactionAfterCommitExecutor).execute(any(Runnable.class));

        ArgumentCaptor<InviteMemberNotificationEvent> inviteCaptor =
                ArgumentCaptor.forClass(InviteMemberNotificationEvent.class);

        verify(applicationEventPublisher).publishEvent(inviteCaptor.capture());

        InviteMemberNotificationEvent publishedEvent = inviteCaptor.getValue();
        assertThat(publishedEvent.albumId()).isEqualTo(albumId);
        assertThat(publishedEvent.actorId()).isEqualTo(inviteTestUser.getId());
    }

    @Test
    void acceptInvite_구성원_초과_예외() {
        // given
        when(redisInviteTokenRepository.existsByToken(token)).thenReturn(true);
        when(jwtTokenProvider.validateAndExtractInviteId(token)).thenReturn(albumId);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(testAlbum));
        when(securityUtil.getCurrentUser()).thenReturn(inviteTestUser);

        List<UserAlbum> fullMembers = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            fullMembers.add(UserAlbum.of(
                    User.builder().id((long) i).nickname("user" + i).build(),
                    testAlbum, UserAlbumRole.NORMAL));
        }
        when(userAlbumRepository.findAllByAlbum(testAlbum)).thenReturn(fullMembers);

        // when & then
        assertThatThrownBy(() -> albumService.acceptInvite(token))
                .isInstanceOf(AlbumException.class)
                .hasMessageContaining("앨범 구성원 정원 초과입니다.");
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

    @Test
    void transferAlbumOwner_정상이양() {
        //given
        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        when(userRepository.findById(member1.getId())).thenReturn(Optional.of(member1));
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(testAlbum));
        when(userAlbumRepository.findByUserAndAlbum(testUser, testAlbum)).thenReturn(Optional.of(testUserAlbum));
        when(userAlbumRepository.findByUserAndAlbum(member1, testAlbum)).thenReturn(Optional.of(member1UA));

        //when
        BaseApiResponse<AlbumOwnerTransferResponseDTO> result = albumService.transferAlbumOwner(albumId, member1.getId());

        //then
        assertThat(result.getData().oldOwnerId()).isEqualTo(testUser.getId());
        assertThat(result.getData().newOwnerId()).isEqualTo(member1.getId());

        assertThat(testUserAlbum.getRole()).isEqualTo(UserAlbumRole.NORMAL);
        assertThat(member1UA.getRole()).isEqualTo(UserAlbumRole.OWNER);
    }

    @Test
    void transferAlbumOwner_OWNER아님 () {
        //given
        when(securityUtil.getCurrentUser()).thenReturn(member1);
        when(userRepository.findById(member2.getId())).thenReturn(Optional.of(member2));
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(testAlbum));
        when(userAlbumRepository.findByUserAndAlbum(member1, testAlbum)).thenReturn(Optional.of(member1UA));
        when(userAlbumRepository.findByUserAndAlbum(member2, testAlbum)).thenReturn(Optional.of(member2UA));

        //when then
        assertThatThrownBy(() -> albumService.transferAlbumOwner(albumId, member2.getId()))
                .isInstanceOf(AlbumException.class)
                .hasMessageContaining("현재 OWNER만 소유권을 위임할 수 있습니다.");
    }

    @Test
    void tranferAlbumOwner_이양유저없음() {
        //givne
        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        when(userRepository.findById(member1.getId())).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> albumService.transferAlbumOwner(albumId, member1.getId()))
                .isInstanceOf(AlbumException.class)
                .hasMessageContaining("이양할 유저 정보를 찾을 수 없습니다.");
    }

    @Test
    void getAlbumRole_정상조회_OWNER() {
        //given
        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(testAlbum));
        when(userAlbumRepository.findByUserAndAlbum(testUser, testAlbum)).thenReturn(Optional.of(testUserAlbum));

        //when
        BaseApiResponse<AlbumRoleResponseDTO> result = albumService.getAlbumRole(albumId);

        //then
        assertThat(result.getData().role()).isEqualTo(UserAlbumRole.OWNER);
    }

    @Test
    void getAlbumRole_정상조회_NORMAL() {
        //given
        when(securityUtil.getCurrentUser()).thenReturn(member1);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(testAlbum));
        when(userAlbumRepository.findByUserAndAlbum(member1, testAlbum)).thenReturn(Optional.of(member1UA));

        //when
        BaseApiResponse<AlbumRoleResponseDTO> result = albumService.getAlbumRole(albumId);

        //then
        assertThat(result.getData().role()).isEqualTo(UserAlbumRole.NORMAL);
    }

    @Test
    void getAlbumRole_접근권한없음() {
        //givne
        when(securityUtil.getCurrentUser()).thenReturn(inviteTestUser);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(testAlbum));

        //when then
        assertThatThrownBy(() -> albumService.getAlbumRole(albumId))
                .isInstanceOf(AlbumException.class)
                .hasMessageContaining("앨범 접근 권한이 없습니다.");
    }

}
