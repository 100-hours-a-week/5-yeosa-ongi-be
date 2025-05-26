package ongi.ongibe.domain.album.event;

import java.util.List;

public record AlbumRetryEvent(
        Long albumId,
        List<String> pictureS3Keys
) {}
