package ongi.ongibe.domain.album.event;

import java.util.List;

public record AlbumNameChangeEvent(String yearMonth, List<Long> userIds) implements AlbumChangeEvent { }
