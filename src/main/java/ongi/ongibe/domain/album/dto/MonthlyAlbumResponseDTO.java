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
public class MonthlyAlbumResponseDTO {
    private String code;
    private String message;
    private AlbumData albumData;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlbumData {
        private List<AlbumInfo> albumInfo;
        private String nextYearMonth;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlbumInfo {
        private Long albumId;
        private String albumName;
        private String thumbnailPictureURL;
        private LocalDateTime createdAt;
        private List<String> memberProfileImageURL;
    }
}
