package ongi.ongibe.domain.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.auth.OAuthProvider;
import ongi.ongibe.domain.auth.dto.KakaoIdTokenPayloadDTO;
import ongi.ongibe.domain.auth.dto.KakaoLoginResponseDTO;
import ongi.ongibe.domain.auth.dto.KakaoTokenResponseDTO;
import ongi.ongibe.domain.auth.entity.OAuthToken;
import ongi.ongibe.domain.auth.repository.OAuthTokenRepository;
import ongi.ongibe.domain.auth.repository.RefreshTokenRepository;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.util.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    @Value("${spring.kakao.auth.client}")
    private String clientId;

    @Value("${spring.kakao.auth.redirect}")
    private String redirectUri;

    private final UserRepository userRepository;
    private final OAuthTokenRepository oauthTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";

    @Transactional
    public KakaoLoginResponseDTO kakaoLogin(String code) {
        //access token, refresh token 요청
        KakaoTokenResponseDTO tokenResponse = getToken(code);
        //id token 파싱
        KakaoIdTokenPayloadDTO userInfo = parseIdToken(tokenResponse.getId_token());
        log.info("카카오 사용자 정보: email={}, nickname={}", userInfo.getEmail(), userInfo.getNickname());

        Optional<User> optionalUser = userRepository.findByProviderId(userInfo.getSub());
        boolean isNewUser = false;
        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            String newNickname = "user_" + UUID.randomUUID().toString().substring(0, 5);
            user = userRepository.save(User.builder()
                    .provider(OAuthProvider.KAKAO)
                    .providerId(userInfo.getSub())
                    .nickname(newNickname)
                    .profileImage(userInfo.getPicture())
                    .email(userInfo.getEmail())
                    .build());
            isNewUser = true;
        }

        OAuthToken oAuthToken = OAuthToken.builder()
                .user(user)
                .provider(OAuthProvider.KAKAO)
                .accessToken(tokenResponse.getAccess_token())
                .refreshToken(tokenResponse.getRefresh_token())
                .accessTokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpires_in()))
                .refreshTokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getRefresh_token_expires_in()))
                .build();
        oauthTokenRepository.save(oAuthToken);

        String ongiAccessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String ongiRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        refreshTokenRepository.save(user.getId(), ongiRefreshToken, 14 * 24 * 60 * 60L);


        return KakaoLoginResponseDTO.builder()
                .code(isNewUser ? "USER_REGISTERED" : "USER_ALREADY_REGISTERED")
                .accessToken(ongiAccessToken)
                .refreshToken(ongiRefreshToken)
                .refreshTokenExpiresIn(60*60*24*14)
                .user(KakaoLoginResponseDTO.UserInfo.builder()
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .profileImageURL(user.getProfileImage())
                        .cacheTtl(300)
                        .build())
                .build();

    }

    private KakaoTokenResponseDTO getToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                KAKAO_TOKEN_URL,
                HttpMethod.POST,
                request,
                String.class
        );
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response.getBody(), KakaoTokenResponseDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("카카오 토큰 응답 파싱 실패", e);
        }
    }

    private KakaoIdTokenPayloadDTO parseIdToken(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(payloadJson, KakaoIdTokenPayloadDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("ID 토큰 파싱 실패", e);
        }
    }
}
