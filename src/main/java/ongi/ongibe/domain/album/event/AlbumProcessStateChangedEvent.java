package ongi.ongibe.domain.album.event;

import ongi.ongibe.domain.album.AlbumProcessState;

public record AlbumProcessStateChangedEvent(
   AlbumProcessState newState
) {}
