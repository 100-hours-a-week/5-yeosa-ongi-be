package ongi.ongibe.domain.auth.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.auth.OAuthProvider;
import ongi.ongibe.domain.auth.dto.KakaoIdTokenPayloadDTO;
import ongi.ongibe.domain.auth.dto.KakaoLoginResponseDTO;
import ongi.ongibe.domain.auth.dto.KakaoTokenResponseDTO;
import ongi.ongibe.domain.auth.dto.RefreshAccessTokenResponseDTO;
import ongi.ongibe.domain.auth.entity.OAuthToken;
import ongi.ongibe.domain.auth.repository.OAuthTokenRepository;
import ongi.ongibe.domain.auth.repository.RefreshTokenRepository;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.exception.InvalidTokenException;
import ongi.ongibe.global.exception.TokenNotFoundException;
import ongi.ongibe.global.exception.TokenParsingException;
import ongi.ongibe.util.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    @Value("${spring.kakao.auth.client}")
    private String clientId;

    @Value("${spring.kakao.auth.redirect}")
    private String redirectUri;

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final OAuthTokenRepository oauthTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";

    @Transactional
    public BaseApiResponse<KakaoLoginResponseDTO> kakaoLogin(String code) {
        //access token, refresh token 요청
        KakaoTokenResponseDTO tokenResponse = getToken(code);
        //id token 파싱
        KakaoIdTokenPayloadDTO kakaoUserInfo = parseIdToken(tokenResponse.id_token());
        log.info("카카오 사용자 정보: email={}, nickname={}", kakaoUserInfo.email(), kakaoUserInfo.nickname());

        Optional<User> optionalUser = userRepository.findByProviderId(kakaoUserInfo.sub());
        boolean isNewUser = false;
        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            String newNickname = "user_" + UUID.randomUUID().toString().substring(0, 5);
            user = userRepository.save(User.builder()
                    .provider(OAuthProvider.KAKAO)
                    .providerId(kakaoUserInfo.sub())
                    .nickname(newNickname)
                    .profileImage(kakaoUserInfo.picture())
                    .email(kakaoUserInfo.email())
                    .build());
            isNewUser = true;
        }

        OAuthToken oAuthToken = OAuthToken.builder()
                .user(user)
                .provider(OAuthProvider.KAKAO)
                .accessToken(tokenResponse.access_token())
                .refreshToken(tokenResponse.refresh_token())
                .accessTokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.expires_in()))
                .refreshTokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.refresh_token_expires_in()))
                .build();
        oauthTokenRepository.save(oAuthToken);

        String ongiAccessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String ongiRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        refreshTokenRepository.save(user.getId(), ongiRefreshToken, 14 * 24 * 60 * 60L);

        KakaoLoginResponseDTO kakaoLoginResponseDTO = KakaoLoginResponseDTO.of(
                ongiAccessToken,
                ongiRefreshToken,
                60 * 60 * 24 * 14,
                user.getId(),
                user.getNickname(),
                user.getProfileImage(),
                300
        );

        return BaseApiResponse.<KakaoLoginResponseDTO>builder()
                .code(isNewUser ? "USER_REGISTERED" : "USER_ALREADY_REGISTERED")
                .message(isNewUser ? "회원가입을 완료했습니다. 로그인을 완료했습니다." : "로그인을 완료했습니다.")
                .data(kakaoLoginResponseDTO)
                .build();
    }

    private KakaoTokenResponseDTO getToken(String code) {
        log.debug("카카오 토큰 요청 시작: code = {}", code);
        log.debug("사용할 redirect_uri = {}", redirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    KAKAO_TOKEN_URL,
                    HttpMethod.POST,
                    request,
                    String.class
            );
            log.info("응답 상태 코드: {}", response.getStatusCode());
            log.info("응답 헤더: {}", response.getHeaders());

            log.info("ResponseEntity 수신 완료");

            String responseBody = response.getBody();

            if (responseBody == null) {
                log.error("응답 바디가 null입니다");
                throw new TokenParsingException("응답 바디가 null입니다");
            }

            log.info("Kakao 응답 원문: {}", responseBody);

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> kakaoResponse = objectMapper.readValue(responseBody, new TypeReference<>() {});
            log.debug("응답 map: {}", kakaoResponse);

            return objectMapper.readValue(responseBody, KakaoTokenResponseDTO.class);

        } catch (HttpClientErrorException e) {
            log.error("카카오 토큰 요청 실패: status = {}, body = {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kakao 인증 실패: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("예상치 못한 예외 발생", e);
            throw new TokenParsingException("카카오 토큰 응답 파싱 실패", e);
        }
    }



    private KakaoIdTokenPayloadDTO parseIdToken(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(payloadJson, KakaoIdTokenPayloadDTO.class);
        } catch (Exception e) {
            throw new TokenParsingException("ID 토큰 파싱 실패", e);
        }
    }

    @Transactional
    public BaseApiResponse<RefreshAccessTokenResponseDTO> reissueAccessToken(String refreshToken) {
        // 1. 리프레시 토큰 검증
        Long userId = jwtTokenProvider.validateAndExtractUserId(refreshToken);

        // 2. 레디스에서 유저 ID로 저장된 리프레시 토큰 조회
        String storedRefreshToken = refreshTokenRepository.findByUserId(userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }

        // 3. 새 AccessToken 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId);

        RefreshAccessTokenResponseDTO refreshAccessTokenResponseDTO = new RefreshAccessTokenResponseDTO(newAccessToken);

        return BaseApiResponse.<RefreshAccessTokenResponseDTO>builder()
                .code("TOKEN_REFRESH_SUCCESS")
                .message("토큰이 재발급되었습니다.")
                .data(refreshAccessTokenResponseDTO)
                .build();
    }


    @Transactional
    public void logout(String authorizationHeader, String refreshToken) {
        String accessToken = extractAccessToken(authorizationHeader);

        // access token 유효성 검사
        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new InvalidTokenException("유효한 access token이 아닙니다.");
        }

        Long userId = jwtTokenProvider.validateAndExtractUserId(accessToken);

        // Redis에서 refresh token 삭제
        String storedRefreshToken = refreshTokenRepository.findByUserId(userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new InvalidTokenException("refresh token이 유효하지 않습니다.");
        }
        refreshTokenRepository.delete(userId);
    }

    private String extractAccessToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new TokenNotFoundException("AccessToken이 없습니다.");
        }
        return authorizationHeader.substring(7);
    }
}