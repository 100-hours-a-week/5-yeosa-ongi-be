package ongi.ongibe.domain.auth.service;

import static org.junit.jupiter.api.Assertions.*;

import ongi.ongibe.domain.auth.config.KakaoOauthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class KakaoOauthClientTest {

    @InjectMocks private KakaoOauthClient kakaoOauthClient;

    @Mock private WebClient webClient;
    @Mock private KakaoOauthProperties kakaoOauthProperties;
    @Mock private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock private WebClient.RequestBodySpec requestBodySpec;
    @Mock private WebClient.RequestHeadersSpec<?> requestHeadersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        kakaoOauthClient = new KakaoOauthClient(webClient,  kakaoOauthProperties);

    }
}