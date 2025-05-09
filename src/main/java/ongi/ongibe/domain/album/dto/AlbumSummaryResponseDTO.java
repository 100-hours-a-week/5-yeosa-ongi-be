package ongi.ongibe.domain.album.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public record AlbumSummaryResponseDTO(
        @Schema(description = "사진 id") Long pictureId,
        @Schema(description = "사진 url") String pictureURL,
        @Schema(description = "사진 위도") Double latitude,
        @Schema(description = "사진 경도") Double longitude
) {}
