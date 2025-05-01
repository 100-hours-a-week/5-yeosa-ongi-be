package ongi.ongibe.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public record RefreshAccessTokenResponseDTO(
        String accessToken
){}
