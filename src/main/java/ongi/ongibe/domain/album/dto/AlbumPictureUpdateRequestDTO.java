package ongi.ongibe.domain.album.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record AlbumPictureUpdateRequestDTO(
        @Schema(description = "상태를 변경할 사진 url") List<Long> pictureIds
) {}
