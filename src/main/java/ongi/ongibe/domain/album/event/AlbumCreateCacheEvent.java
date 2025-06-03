package ongi.ongibe.domain.album.event;

import java.util.List;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.util.DateUtil;

public record AlbumCreateCacheEvent(String yearMonth, List<Long> userIds) implements AlbumChangeEvent{}
