package ongi.ongibe.domain.album.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.AlbumInviteResponseDTO;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Spy
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

    private final Long albumId = 1L;
    private final String token = "mocked.jwt.token";

    private Album testAlbum;
    private User testUser;

    @BeforeEach
    void setUp() {
        testAlbum = Album.builder()
                .id(albumId)
                .name("여행 앨범")
                .pictures(new ArrayList<>())
                .userAlbums(new ArrayList<>())
                .build();

        testUser = User.builder()
                .id(123L)
                .nickname("test-user")
                .providerId("abc123")
                .provider(OAuthProvider.KAKAO)
                .userStatus(UserStatus.ACTIVE)
                .profileImage("image.png")
                .userAlbums(new ArrayList<>())
                .build();
    }

    @Test
    void createInviteToken_성공() {
        // given
        // getAlbumIfMember, validAlbumOwner 는 실 코드에서 직접 호출한다고 가정
        doReturn(testAlbum).when(albumService).getAlbumIfMember(albumId);
        doNothing().when(albumService).validAlbumOwner(testAlbum);
        when(jwtTokenProvider.generateInviteToken(albumId)).thenReturn(token);

        // when
        BaseApiResponse<String> result = albumService.createInviteToken(albumId);

        // then
        assertThat(result.getData()).isEqualTo("https://ongi.com/invite?token=" + token);
        verify(redisInviteTokenRepository).save(token, albumId);
    }

    @Test
    void acceptInvite_성공() {
        // given
        when(redisInviteTokenRepository.existsByToken(token)).thenReturn(true);
        when(jwtTokenProvider.validateAndExtractInviteId(token)).thenReturn(albumId);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(testAlbum));
        when(securityUtil.getCurrentUser()).thenReturn(testUser);

        // when
        BaseApiResponse<AlbumInviteResponseDTO> result = albumService.acceptInvite(token);

        // then
        assertThat(result.getData().albumId()).isEqualTo(albumId);
        assertThat(result.getData().albumName()).isEqualTo("여행 앨범");
        verify(userAlbumRepository).save(any(UserAlbum.class));
        verify(redisInviteTokenRepository).remove(token);
    }

    @Test
    void acceptInvite_토큰없음_예외() {
        // given
        when(redisInviteTokenRepository.existsByToken(token)).thenReturn(false);

        // expect
        assertThatThrownBy(() -> albumService.acceptInvite(token))
                .isInstanceOf(AlbumException.class)
                .hasMessageContaining("초대코드");
    }
}
