package ongi.ongibe.domain.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.auth.dto.KakaoIdTokenPayloadDTO;
import ongi.ongibe.domain.auth.dto.KakaoTokenResponseDTO;
import ongi.ongibe.global.exception.TokenParsingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOauthClient {

    @Value("${spring.kakao.auth.client}")
    private String clientId;

    @Value("${spring.kakao.auth.redirect}")
    private String redirectUri;

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";

    private final WebClient webClient;

    public KakaoTokenResponseDTO getToken(String code) {
        log.debug("카카오 토큰 요청 시작: code = {}", code);
        log.debug("사용할 redirect_uri = {}", redirectUri);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);

        try {
            String responseBody = webClient.post()
                    .uri(KAKAO_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .onStatus(statusCode -> statusCode.is4xxClientError() || statusCode.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(body -> new ResponseStatusException(clientResponse.statusCode(), "Kakao 인증 실패: " + body)))
                    .bodyToMono(String.class)
                    .block();

            log.info("Kakao 응답 원문: {}", responseBody);

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseBody, KakaoTokenResponseDTO.class);

        } catch (Exception e) {
            log.error("카카오 토큰 요청 실패", e);
            throw new TokenParsingException("카카오 토큰 응답 파싱 실패", e);
        }
    }

    public KakaoIdTokenPayloadDTO parseIdToken(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(payloadJson, KakaoIdTokenPayloadDTO.class);
        } catch (Exception e) {
            throw new TokenParsingException("ID 토큰 파싱 실패", e);
        }
    }

}
