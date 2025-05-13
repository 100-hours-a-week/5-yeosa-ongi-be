package ongi.ongibe.global.s3.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record PresignedUrlResponseDTO(
        @Schema(description = "사진 리스트") List<PresignedFile> presignedFiles
) {
    public record PresignedFile(
            @Schema(description = "사진 이름") String pictureName,
            @Schema(description = "presigned url") String presignedUrl,
            @Schema(description = "S3 url") String pictureURL) {}
}