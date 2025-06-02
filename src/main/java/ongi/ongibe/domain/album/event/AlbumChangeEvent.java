package ongi.ongibe.domain.album.event;

import java.util.List;

public interface AlbumChangeEvent {
    String yearMonth();
    List<Long> userIds();
}
