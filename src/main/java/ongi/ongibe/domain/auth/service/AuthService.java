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
import ongi.ongibe.global.s3.PresignedUrlService;
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
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final KakaoOauthClient kakaoOauthClient;
    private final UserRepository userRepository;
    private final OAuthTokenRepository oauthTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PresignedUrlService presignedUrlService;

    @Transactional
    public BaseApiResponse<KakaoLoginResponseDTO> kakaoLogin(String code) {
        //access token, refresh token 요청
        KakaoTokenResponseDTO tokenResponse = kakaoOauthClient.getToken(code);
        //id token 파싱
        KakaoIdTokenPayloadDTO kakaoUserInfo = kakaoOauthClient.parseIdToken(tokenResponse.id_token());
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

        String key = user.getS3Key() == null ?
                presignedUrlService.extractS3Key(user.getProfileImage()) : user.getS3Key();
        String presignedProfile = presignedUrlService.generateGetPresignedUrl(key);

        KakaoLoginResponseDTO kakaoLoginResponseDTO = KakaoLoginResponseDTO.of(
                ongiAccessToken,
                ongiRefreshToken,
                60 * 60 * 24 * 14,
                user.getId(),
                user.getNickname(),
                presignedProfile,
                300
        );

        return BaseApiResponse.<KakaoLoginResponseDTO>builder()
                .code(isNewUser ? "USER_REGISTERED" : "USER_ALREADY_REGISTERED")
                .message(isNewUser ? "회원가입을 완료했습니다. 로그인을 완료했습니다." : "로그인을 완료했습니다.")
                .data(kakaoLoginResponseDTO)
                .build();
    }

    @Transactional
    public BaseApiResponse<RefreshAccessTokenResponseDTO> reissueAccessToken(String refreshToken) {
        log.info("refreshToken: {}", refreshToken);
        // 1. 리프레시 토큰 검증
        Long userId = jwtTokenProvider.validateAndExtractUserId(refreshToken);
        log.info("userId: {}", userId);

        // 2. 레디스에서 유저 ID로 저장된 리프레시 토큰 조회
        String storedRefreshToken = refreshTokenRepository.findByUserId(userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            log.warn("유효하지 않은 토큰입니다 : {}", refreshToken);
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        }

        // 3. 새 AccessToken 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId);
        log.info("newAccessToken: {}", newAccessToken);

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