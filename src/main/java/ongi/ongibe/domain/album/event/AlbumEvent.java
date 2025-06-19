package ongi.ongibe.domain.album.event;

import java.util.List;

public record AlbumEvent(
        Long albumId,
        Long userId,
        List<String> pictureS3Keys
) {}
