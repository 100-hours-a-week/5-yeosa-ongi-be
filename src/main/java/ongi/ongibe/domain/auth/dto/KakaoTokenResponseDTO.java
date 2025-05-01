package ongi.ongibe.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

public record KakaoTokenResponseDTO(
    String token_type,
    String access_token,
    String id_token,
    int expires_in,
    String refresh_token,
    int refresh_token_expires_in,
    String scope
){}
