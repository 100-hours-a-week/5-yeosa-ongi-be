package ongi.ongibe.domain.album.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumSummaryResponseDTO {
    private Long pictureId;
    private String pictureURL;
    private double latitude;
    private double longitude;
}
