package ongi.ongibe.domain.album.dto;

import java.util.List;

public record AlbumCreateRequestDTO(
        String albumName,
        List<String> pictureUrls
) { }
