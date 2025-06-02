package ongi.ongibe.domain.album.event;

import ongi.ongibe.domain.album.entity.Picture;

public record AlbumThumbnailChangedEvent(
        Picture newThumbnail
) {}
