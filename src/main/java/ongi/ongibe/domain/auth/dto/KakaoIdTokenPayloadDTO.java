package ongi.ongibe.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoIdTokenPayloadDTO {
    private String iss;
    private String aud;
    private String sub;
    private long iat;
    private long exp;
    private long auth_time;
    private String nonce;

    private String nickname;
    private String picture;
    private String email;
}

