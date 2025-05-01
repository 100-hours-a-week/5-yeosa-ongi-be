package ongi.ongibe.domain.user.dto;

import java.util.List;

public record UserTotalStateResponseDTO(
        List<PictureCoordinate> pictureCoordinates,
        int albumCount,
        int placeCount
) {
    public record PictureCoordinate(
            double latitude,
            double longitude
    ) {}
}
