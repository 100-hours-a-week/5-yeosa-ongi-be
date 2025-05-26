package ongi.ongibe.domain.album.dto;

import ongi.ongibe.UserAlbumRole;
import ongi.ongibe.domain.album.entity.UserAlbum;

public record AlbumRoleResponseDTO(
        UserAlbumRole role
) {}
