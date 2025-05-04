package ongi.ongibe.domain.album.dto;

import java.util.List;
import ongi.ongibe.UserAlbumRole;

public record AlbumMemberResponseDTO(
        List<UserInfo> userInfo
) {
    public record UserInfo(
            Long userId, String nickname, UserAlbumRole role, String profileImageURL
    ){}
}
