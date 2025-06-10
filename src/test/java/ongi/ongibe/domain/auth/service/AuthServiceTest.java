package ongi.ongibe.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.auth.OAuthProvider;
import ongi.ongibe.domain.auth.dto.KakaoIdTokenPayloadDTO;
import ongi.ongibe.domain.auth.dto.KakaoLoginResponseDTO;
import ongi.ongibe.domain.auth.dto.KakaoTokenResponseDTO;
import ongi.ongibe.domain.auth.dto.RefreshAccessTokenResponseDTO;
import ongi.ongibe.domain.auth.repository.OAuthTokenRepository;
import ongi.ongibe.domain.auth.repository.RefreshTokenRepository;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.exception.InvalidTokenException;
import ongi.ongibe.global.exception.TokenNotFoundException;
import ongi.ongibe.global.exception.TokenParsingException;
import ongi.ongibe.global.s3.PresignedUrlService;
import ongi.ongibe.util.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks private AuthService authService;

    @Mock private UserRepository userRepository;
    @Mock private OAuthTokenRepository oauthTokenRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PresignedUrlService presignedUrlService;
    @Mock private KakaoOauthClient kakaoOauthClient;


    private final String kakaoSub = "1234567890";
    private final String code = "auth-code";
    private KakaoTokenResponseDTO tokenResponse;
    private KakaoIdTokenPayloadDTO idTokenPayload;
    private User mockUser;
    private final String accessToken = "validAccessToken";
    private final String refreshToken = "validRefreshToken";
    private final String authorizationHeader = "Bearer " + accessToken;
    private final Long userId = 123L;

    @BeforeEach
    void setUp() {
        tokenResponse = new KakaoTokenResponseDTO(
                "Bearer",
                "access-token",
                "mock-id-token",
                3600,
                "refresh-token",
                1209600,
                "scope"
        );
        System.out.println(tokenResponse.id_token());

        idTokenPayload = new KakaoIdTokenPayloadDTO(
                "https://kauth.kakao.com", // iss
                "client-id", // aud
                kakaoSub, // sub
                1710000000L, // iat
                1713600000L, // exp
                1710000000L, // auth_time
                "some-nonce", // nonce
                "홍길동", // nickname
                "https://image.com/profile.jpg", // picture
                "user@email.com" // email
        );

        mockUser = User.builder()
                .id(123L)
                .provider(OAuthProvider.KAKAO)
                .providerId(kakaoSub)
                .nickname("user_nick")
                .profileImage("https://image.com/profile.jpg")
                .email("user@email.com")
                .build();
    }

    @Test
    void kakaoLogin_신규가입() {
        //given
        when(userRepository.findByProviderId(kakaoSub)).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(mockUser);

        when(kakaoOauthClient.getToken(code)).thenReturn(tokenResponse);
        when(kakaoOauthClient.parseIdToken(tokenResponse.id_token())).thenReturn(idTokenPayload);

        when(jwtTokenProvider.generateAccessToken(anyLong())).thenReturn("ongi-access-token");
        when(jwtTokenProvider.generateRefreshToken(anyLong())).thenReturn("ongi-refresh-token");

        when(presignedUrlService.extractS3Key(any())).thenReturn("key.img");

        BaseApiResponse<KakaoLoginResponseDTO> response = authService.kakaoLogin(code);
        KakaoLoginResponseDTO data = response.getData();

        assertThat(response.getCode()).isEqualTo("USER_REGISTERED");
        assertThat(data.user().userId()).isEqualTo(mockUser.getId());
        assertThat(data.accessToken()).isEqualTo("ongi-access-token");
    }

    @Test
    void kakaoLogin_기존가입자_성공() {
        //given
        when(userRepository.findByProviderId(kakaoSub)).thenReturn(Optional.ofNullable(mockUser));

        when(kakaoOauthClient.getToken(code)).thenReturn(tokenResponse);
        when(kakaoOauthClient.parseIdToken(tokenResponse.id_token())).thenReturn(idTokenPayload);

        when(jwtTokenProvider.generateAccessToken(anyLong())).thenReturn("ongi-access-token");
        when(jwtTokenProvider.generateRefreshToken(anyLong())).thenReturn("ongi-refresh-token");

        when(presignedUrlService.extractS3Key(any())).thenReturn("key.img");

        //when
        BaseApiResponse<KakaoLoginResponseDTO> response = authService.kakaoLogin(code);
        KakaoLoginResponseDTO data = response.getData();
        //then
        assertThat(response.getCode()).isEqualTo("USER_ALREADY_REGISTERED");
        assertThat(data.user().userId()).isEqualTo(mockUser.getId());
        assertThat(data.accessToken()).isEqualTo("ongi-access-token");
    }

    @Test
    void kakaoLogin_로그인실패_카카오토큰요청실패(){
        //given
        when(kakaoOauthClient.getToken(code)).thenThrow(new TokenParsingException("토큰 파싱 실패"));

        //when, then
        assertThatThrownBy(() -> authService.kakaoLogin(code))
                .isInstanceOf(TokenParsingException.class)
                .hasMessageContaining("실패");
    }

    @Test
    void kakaoLogin_로그인실패_토큰파싱실패(){
        //givn
        when(kakaoOauthClient.getToken(code)).thenReturn(tokenResponse);
        when(kakaoOauthClient.parseIdToken(tokenResponse.id_token())).thenThrow(new TokenParsingException("토큰 파싱 실패"));

        //when, then
        assertThatThrownBy(() -> authService.kakaoLogin(code))
                .isInstanceOf(TokenParsingException.class)
                .hasMessageContaining("파싱 실패");
    }

    @Test
    void reissueAccessToken_성공() {
        when(jwtTokenProvider.validateAndExtractUserId("refresh-token")).thenReturn(1L);
        when(refreshTokenRepository.findByUserId(1L)).thenReturn("refresh-token");
        when(jwtTokenProvider.generateAccessToken(1L)).thenReturn("new-access-token");

        BaseApiResponse<RefreshAccessTokenResponseDTO> result = authService.reissueAccessToken("refresh-token");
        assertThat(result.getCode()).isEqualTo("TOKEN_REFRESH_SUCCESS");
        assertThat(result.getData().accessToken()).isEqualTo("new-access-token");
    }

    @Test
    void reissueAccessToken_refreshToken검증실패(){
        when(jwtTokenProvider.validateAndExtractUserId("fake-refresh-token")).thenThrow(new InvalidTokenException("토큰이 유효하지 않습니다"));

        assertThatThrownBy(() -> authService.reissueAccessToken("fake-refresh-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void reissueAccessToken_refreshToken조회실패(){
        when(jwtTokenProvider.validateAndExtractUserId("refresh-token")).thenReturn(1L);
        when(refreshTokenRepository.findByUserId(1L)).thenThrow(new InvalidTokenException("토큰을 찾을 수 없습니다."));

        assertThatThrownBy(() -> authService.reissueAccessToken("refresh-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void logout_정상() {
        when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
        when(jwtTokenProvider.validateAndExtractUserId(accessToken)).thenReturn(userId);
        when(refreshTokenRepository.findByUserId(userId)).thenReturn(refreshToken);

        authService.logout(authorizationHeader, refreshToken);

        verify(refreshTokenRepository).delete(userId);
    }

    @Test
    void logout_잘못된AuthorizationHeader_예외() {
        assertThatThrownBy(() -> authService.logout("BadToken", refreshToken))
                .isInstanceOf(TokenNotFoundException.class)
                .hasMessage("AccessToken이 없습니다.");
    }

    @Test
    void logout_유효하지않은AccessToken_예외() {
        when(jwtTokenProvider.validateToken(accessToken)).thenReturn(false);

        assertThatThrownBy(() -> authService.logout(authorizationHeader, refreshToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("유효한 access token이 아닙니다.");
    }

    @Test
    void logout_저장된RefreshToken과다름_예외() {
        when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
        when(jwtTokenProvider.validateAndExtractUserId(accessToken)).thenReturn(userId);
        when(refreshTokenRepository.findByUserId(userId)).thenReturn("differentToken");

        assertThatThrownBy(() -> authService.logout(authorizationHeader, refreshToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("refresh token이 유효하지 않습니다.");
    }

    @Test
    void logout_refreshToken_저장안됨_예외() {
        when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
        when(jwtTokenProvider.validateAndExtractUserId(accessToken)).thenReturn(userId);
        when(refreshTokenRepository.findByUserId(userId)).thenReturn(null);

        assertThatThrownBy(() -> authService.logout(authorizationHeader, refreshToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("refresh token이 유효하지 않습니다.");
    }
}