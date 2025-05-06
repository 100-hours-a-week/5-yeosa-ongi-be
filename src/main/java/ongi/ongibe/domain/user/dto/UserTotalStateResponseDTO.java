package ongi.ongibe.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record UserTotalStateResponseDTO(
        @Schema(description = "사진의 좌표 목록") List<PictureCoordinate> pictureCoordinates,
        @Schema(description = "총 앨범 수") int albumCount,
        @Schema(description = "총 방문 장소 수") int placeCount
) {
    public record PictureCoordinate(
            @Schema(description = "위도") double latitude,
            @Schema(description = "경도") double longitude
    ) {}
}