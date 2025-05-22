package ongi.ongibe.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.Optional;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.auth.OAuthProvider;
import ongi.ongibe.domain.auth.dto.KakaoIdTokenPayloadDTO;
import ongi.ongibe.domain.auth.dto.KakaoLoginResponseDTO;
import ongi.ongibe.domain.auth.dto.KakaoTokenResponseDTO;
import ongi.ongibe.domain.auth.repository.OAuthTokenRepository;
import ongi.ongibe.domain.auth.repository.RefreshTokenRepository;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.s3.PresignedUrlService;
import ongi.ongibe.util.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock private UserRepository userRepository;
    @Mock private OAuthTokenRepository oauthTokenRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PresignedUrlService presignedUrlService;
    @Mock private WebClient webClient;
    @Mock private KakaoOauthClient kakaoOauthClient;


    private final String kakaoSub = "1234567890";
    private final String code = "auth-code";
    private KakaoTokenResponseDTO tokenResponse;
    private KakaoIdTokenPayloadDTO idTokenPayload;
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        tokenResponse = new KakaoTokenResponseDTO(
                "Bearer",
                "access-token",
                "mock-id-token",
                3600,
                "refresh-token",
                1209600,
                "scope"
        );

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
                .id(1L)
                .provider(OAuthProvider.KAKAO)
                .providerId(kakaoSub)
                .nickname("user_nick")
                .profileImage("https://image.com/profile.jpg")
                .email("user@email.com")
                .build();

        when(jwtTokenProvider.generateAccessToken(anyLong())).thenReturn("ongi-access-token");
        when(jwtTokenProvider.generateRefreshToken(anyLong())).thenReturn("ongi-refresh-token");

        when(presignedUrlService.extractS3Key(any())).thenReturn("key.img");
        when(presignedUrlService.generateGetPresignedUrl("key.img")).thenReturn("https://presigned.url");
    }

    @Test
    void kakaoLogin_신규가입() {
        //given
        when(userRepository.findByProviderId(kakaoSub)).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(mockUser);

        when(kakaoOauthClient.getToken(code)).thenReturn(tokenResponse);
        when(kakaoOauthClient.parseIdToken(tokenResponse.id_token())).thenReturn(idTokenPayload);

        BaseApiResponse<KakaoLoginResponseDTO> response = authService.kakaoLogin(code);
        KakaoLoginResponseDTO data = response.getData();

        assertThat(response.getCode()).isEqualTo("USER_REGISTERED");
        assertThat(data.user().userId()).isEqualTo(mockUser.getId());
        assertThat(data.accessToken()).isEqualTo("ongi-access-token");
    }

    @Test
    void kakaoLogin_로그인실패_카카오토큰요청실패(){

    }

    @Test
    void reissueAccessToken() {

    }

    @Test
    void logout() {
    }
}