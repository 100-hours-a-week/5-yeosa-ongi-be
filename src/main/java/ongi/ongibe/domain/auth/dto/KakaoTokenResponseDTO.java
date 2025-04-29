package ongi.ongibe.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoTokenResponseDTO {

    private String token_type;
    private String access_token;
    private String id_token; // JWT
    private int expires_in;
    private String refresh_token;
    private int refresh_token_expires_in;
    private String scope;
}
