package ongi.ongibe.domain.album.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AlbumInviteResponseDTO(
        @Schema(description = "앨범 id") Long albumId,
        @Schema(description = "앨범 이름") String albumName
) {}
