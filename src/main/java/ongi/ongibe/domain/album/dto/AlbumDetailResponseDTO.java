package ongi.ongibe.domain.album.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AlbumDetailResponseDTO(
        String title,
        List<PictureInfo> picture
) {
    public record PictureInfo(
            Long pictureId,
            String pictureURL,
            double latitude,
            double longitude,
            String tag,
            float qualityScore,
            boolean isDuplicated,
            boolean isShaky,
            LocalDateTime takeAt
    ) {}
}
