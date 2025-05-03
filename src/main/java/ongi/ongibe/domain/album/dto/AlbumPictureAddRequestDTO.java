package ongi.ongibe.domain.album.dto;

import java.util.List;

public record AlbumPictureAddRequestDTO(
        List<String> pictureUrls
) { }
