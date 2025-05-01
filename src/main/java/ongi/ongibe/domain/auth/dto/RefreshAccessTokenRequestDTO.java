package ongi.ongibe.domain.auth.dto;

import lombok.Getter;
import lombok.Setter;

public record RefreshAccessTokenRequestDTO(
        String refreshToken
) {}
