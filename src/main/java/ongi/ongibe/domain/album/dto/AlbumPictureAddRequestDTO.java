package ongi.ongibe.domain.album.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record AlbumPictureAddRequestDTO(
        @Schema(description = "사진 url") List<String> pictureUrls
) { }
