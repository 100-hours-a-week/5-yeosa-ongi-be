package ongi.ongibe.global.s3.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record PresignedUrlRequestDTO(
        @Schema(description = "사진 리스트") List<PictureInfo> pictures
) {
    public record PictureInfo(
            @Schema(description = "사진 이름") String pictureName,
            @Schema(description = "사진 타입") String pictureType
    ) {}
}
