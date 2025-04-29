package ongi.ongibe.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RefreshAccessTokenResponseDTO {
    private String code;
    private String message;
    private String accessToken;
}
