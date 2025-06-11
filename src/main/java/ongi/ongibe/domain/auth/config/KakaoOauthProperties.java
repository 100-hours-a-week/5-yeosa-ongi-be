package ongi.ongibe.domain.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.kakao.auth")
@Data
public class KakaoOauthProperties {
    private String client;
    private String redirect;
}
