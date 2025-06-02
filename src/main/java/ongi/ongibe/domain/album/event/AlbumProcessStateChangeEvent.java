package ongi.ongibe.domain.album.event;

import java.util.List;

public record AlbumProcessStateChangeEvent(String yearMonth, List<Long> userIds) implements AlbumChangeEvent {}
