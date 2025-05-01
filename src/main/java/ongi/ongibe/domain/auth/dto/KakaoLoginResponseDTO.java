package ongi.ongibe.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public record KakaoLoginResponseDTO(
        String accessToken,
        int refreshTokenExpiresIn,
        String refreshToken,
        UserInfo user
) {
    public static KakaoLoginResponseDTO of(String accessToken, String refreshToken, int refreshTokenExpiresIn, Long userId, String nickname, String profileImageURL, int cacheTtl) {
        return new KakaoLoginResponseDTO(
                accessToken,
                refreshTokenExpiresIn,
                refreshToken,
                new UserInfo(userId, nickname, profileImageURL, cacheTtl)
        );
    }

    public record UserInfo(
            Long userId,
            String nickname,
            String profileImageURL,
            int cacheTtl
    ) {}
}