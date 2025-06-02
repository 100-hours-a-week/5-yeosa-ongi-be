package ongi.ongibe.domain.album.event;

import ongi.ongibe.domain.album.entity.Album;

public record AlbumThumbnailChangeEvent(Album album) implements AlbumChangeEvent{}