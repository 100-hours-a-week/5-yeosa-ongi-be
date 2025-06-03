package ongi.ongibe.domain.album.dto;

import java.util.List;

public record AlbumCreateRequestGeoFrontDTO(
        String albumName,
        List<PictureRequestDTO> pictureUrls
) {
    public record PictureRequestDTO(
            String pictureUrl,
            Double latitude,
            Double longitude
    ){}
}
