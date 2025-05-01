package ongi.ongibe.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

public record KakaoIdTokenPayloadDTO(
        String iss,
        String aud,
        String sub,
        long iat,
        long exp,
        long auth_time,
        String nonce,
        String nickname,
        String picture,
        String email
) {}