package ongi.ongibe.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ongi.ongibe.domain.user.entity.User;

public record UserInfoResponseDTO (
        @Schema(description = "유저 id") Long userId,
        @Schema(description = "유저 닉네임") String nickname,
        @Schema(description = "프로필 사진 주소") String profileImageURL,
        @Schema(description = "캐시 유효기간") int cacheTil
){
    public static UserInfoResponseDTO of(User user) {
        return new UserInfoResponseDTO(
                user.getId(),
                user.getNickname(),
                user.getProfileImage(),
                300
        );
    }
}

