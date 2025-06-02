package ongi.ongibe.domain.album.event;

import ongi.ongibe.domain.album.entity.Album;

public record AlbumNameChangeEvent(Album album) implements AlbumChangeEvent {

}
