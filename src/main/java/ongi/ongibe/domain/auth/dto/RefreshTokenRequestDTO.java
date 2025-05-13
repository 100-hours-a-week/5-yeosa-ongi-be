package ongi.ongibe.domain.auth.dto;

import lombok.Getter;
import lombok.Setter;

public record RefreshTokenRequestDTO(
        String refreshToken
){}

