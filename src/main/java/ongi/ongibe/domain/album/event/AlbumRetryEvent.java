package ongi.ongibe.domain.album.event;

import java.util.List;

public record AlbumRetryEvent(
        Long albumId,
        Long userId,
        List<String> pictureS3Keys,
        List<String> concepts
) {}
