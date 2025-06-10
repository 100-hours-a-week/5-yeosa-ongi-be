package ongi.ongibe.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Function;
import java.util.function.Supplier;
import ongi.ongibe.domain.auth.config.KakaoOauthProperties;
import ongi.ongibe.domain.auth.dto.KakaoIdTokenPayloadDTO;
import ongi.ongibe.domain.auth.dto.KakaoTokenResponseDTO;
import ongi.ongibe.global.exception.TokenParsingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class KakaoOauthClientTest {

    private KakaoOauthClient kakaoOauthClient;

    @Mock private WebClient webClient;
    @Mock private KakaoOauthProperties kakaoOauthProperties;
    @Mock private RequestBodyUriSpec requestBodyUriSpec;
    @Mock private RequestBodySpec requestBodySpec;
    @Mock private RequestHeadersSpec requestHeadersSpec;
    @Mock private ResponseSpec responseSpec;

    private final String clientId = "dummy-clientId";
    private final String redirectUri = "dummy-redirectUri";

    private final String dummyCode = "kakao-code";
    private final String responseBody = """
            {
              "token_type": "Bearer",
              "access_token": "access-token",
              "id_token": "mock-id-token",
              "expires_in": 3600,
              "refresh_token": "refresh-token",
              "refresh_token_expires_in": 1209600,
              "scope": "account_email profile"
            }
            """;
    private String validIdToken;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        kakaoOauthClient = new KakaoOauthClient(webClient,  kakaoOauthProperties);

        KakaoIdTokenPayloadDTO payload = new KakaoIdTokenPayloadDTO(
                "https://kauth.kakao.com",
                "test-client-id",
                "user-123",
                1620000000L,
                1620003600L,
                1620000000L,
                "test-nonce",
                "테스터",
                "https://profile.com/test.jpg",
                "test@kakao.com"
        );

        ObjectMapper mapper = new ObjectMapper();
        String payloadJson = mapper.writeValueAsString(payload);
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

        validIdToken = "header." + encodedPayload + ".signature";
    }

    @Test
    void getToken_정상(){
        when(kakaoOauthProperties.getClient()).thenReturn(clientId);
        when(kakaoOauthProperties.getRedirect()).thenReturn(redirectUri);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseBody));

        //when
        KakaoTokenResponseDTO token = kakaoOauthClient.getToken(clientId);

        //then
        assertThat(token.access_token()).isEqualTo("access-token");
        assertThat(token.refresh_token()).isEqualTo("refresh-token");
        assertThat(token.id_token()).isEqualTo("mock-id-token");
    }

    @Test
    void getToken_잘못된Json이면_예외발생() {
        // given
        when(kakaoOauthProperties.getClient()).thenReturn("test-client-id");
        when(kakaoOauthProperties.getRedirect()).thenReturn("http://localhost/redirect");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("not-json"));

        // then
        assertThatThrownBy(() -> kakaoOauthClient.getToken("code"))
                .isInstanceOf(TokenParsingException.class)
                .hasMessageContaining("파싱");
    }

    @Test
    void getToken_kakao측_에러코드_400() {
        // given
        when(kakaoOauthProperties.getClient()).thenReturn("test-client-id");
        when(kakaoOauthProperties.getRedirect()).thenReturn("http://localhost/redirect");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(
                Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kakao 인증 실패"))
        );

        // then
        assertThatThrownBy(() -> kakaoOauthClient.getToken("code"))
                .isInstanceOf(TokenParsingException.class)
                .hasMessageContaining("파싱");
    }

    @Test
    void getToken_kakao측_에러코드_500() {
        // given
        when(kakaoOauthProperties.getClient)).thenReturn("test-client-id");
        when(kakaoOauthProperties.getRedirect()).thenReturn("http://localhost/redirect");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(
                Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Kakao 인증 실패"))
        );

        // then
        assertThatThrownBy(() -> kakaoOauthClient.getToken("code"))
                .isInstanceOf(TokenParsingException.class)
                .hasMessageContaining("파싱");
    }

    @Test
    void parseIdToken_정상() {
        // when
        KakaoIdTokenPayloadDTO result = kakaoOauthClient.parseIdToken(validIdToken);

        // then
        assertThat(result.email()).isEqualTo("test@kakao.com");
        assertThat(result.nickname()).isEqualTo("테스터");
        assertThat(result.sub()).isEqualTo("user-123");
    }

    @Test
    void parseIdToken_형식이_잘못된_토큰이면_예외() {
        // given
        String invalidToken = "invalidToken";

        // when then
        assertThatThrownBy(() -> kakaoOauthClient.parseIdToken(invalidToken))
                .isInstanceOf(TokenParsingException.class)
                .hasMessageContaining("ID 토큰 파싱 실패");
    }
}