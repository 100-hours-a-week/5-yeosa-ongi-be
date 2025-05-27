package ongi.ongibe.domain.album.event;

import java.util.List;

public record AlbumClusterEvent(
        Long albumId, List<String> pictureS3Keys
) {}
