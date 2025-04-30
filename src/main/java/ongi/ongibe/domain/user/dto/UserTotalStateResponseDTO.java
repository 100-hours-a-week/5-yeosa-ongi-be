package ongi.ongibe.domain.user.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTotalStateResponseDTO {

    private List<PictureCoordinate> pictureCoordinates;
    private int albumCount;
    private int placeCount;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PictureCoordinate {
        private double latitude;
        private double longitude;
    }
}
