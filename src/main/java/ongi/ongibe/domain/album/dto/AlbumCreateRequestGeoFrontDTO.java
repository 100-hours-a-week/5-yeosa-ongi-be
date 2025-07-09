package ongi.ongibe.domain.album.dto;

import java.util.List;
import ongi.ongibe.domain.album.dto.PictureUrlCoordinateDTO;

public record AlbumCreateRequestGeoFrontDTO(
        String albumName,
        List<PictureRequestDTO> pictureUrls,
        List<String> concepts
) {
    public record PictureRequestDTO(
            String pictureUrl,
            Double latitude,
            Double longitude
    ) implements PictureUrlCoordinateDTO {}
}
