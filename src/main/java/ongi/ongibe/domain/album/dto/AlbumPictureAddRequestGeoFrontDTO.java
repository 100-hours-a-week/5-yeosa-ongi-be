package ongi.ongibe.domain.album.dto;

import java.util.List;
import ongi.ongibe.domain.album.dto.PictureUrlCoordinateDTO;

public record AlbumPictureAddRequestGeoFrontDTO(
        List<PictureRequestDTO> pictureUrls
) {
    public record PictureRequestDTO(
            String pictureUrl,
            Double latitude,
            Double longitude
    ) implements PictureUrlCoordinateDTO {}
}
