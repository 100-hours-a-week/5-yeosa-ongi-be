package ongi.ongibe.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KakaoLoginResponseDTO {
    private String code;
    private String accessToken;
    private int refreshTokenExpiresIn;
    private String refreshToken;
    private UserInfo user;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String nickname;
        private String profileImageURL;
        private int cacheTtl;
    }
}
