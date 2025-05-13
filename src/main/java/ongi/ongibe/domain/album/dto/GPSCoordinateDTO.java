package ongi.ongibe.domain.album.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record GPSCoordinateDTO(
   @Schema(description = "사진 위도") Double lat,
   @Schema(description = "사진 경도") Double lon
) {}
