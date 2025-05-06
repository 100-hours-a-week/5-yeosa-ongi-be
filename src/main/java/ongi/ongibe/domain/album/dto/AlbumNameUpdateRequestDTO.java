package ongi.ongibe.domain.album.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AlbumNameUpdateRequestDTO(
        @Schema(description = "변경할 앨범이름") String albumName
)
{}
