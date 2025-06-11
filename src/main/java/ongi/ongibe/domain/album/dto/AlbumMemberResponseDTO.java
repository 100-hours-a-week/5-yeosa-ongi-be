package ongi.ongibe.domain.album.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import ongi.ongibe.domain.album.UserAlbumRole;

public record AlbumMemberResponseDTO(
        @Schema(description = "유저정보") List<UserInfo> userInfo
) {
    public record UserInfo(
            @Schema(description = "유저 id") Long userId,
            @Schema(description = "유저 닉네임") String nickname,
            @Schema(description = "앨범에서 유저 권한") UserAlbumRole role,
            @Schema(description = "유저 프로필이미지") String profileImageURL
    ){}
}
