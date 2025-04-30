package ongi.ongibe.domain.album.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDetailResponseDTO {

    private String title;
    private List<PictureInfo> picture;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PictureInfo {
        private Long pictureId;
        private String pictureURL;
        private double latitude;
        private double longitude;
        private String tag;
        private float qualityScore;
        private boolean isDuplicated;
        private boolean isShaky;
        private LocalDateTime takeAt;
    }
}