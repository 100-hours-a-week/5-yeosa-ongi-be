package ongi.ongibe.domain.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshAccessTokenRequestDTO {
    private String refreshToken;
}
