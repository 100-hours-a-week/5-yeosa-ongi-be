package ongi.ongibe.domain.album.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record AlbumCreateRequestDTO(
        @Schema(description = "앨범 이름") String albumName,
        @Schema(description = "presigned url로 올린 url들") List<String> pictureUrls
) { }
