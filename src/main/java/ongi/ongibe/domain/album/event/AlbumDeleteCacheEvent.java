package ongi.ongibe.domain.album.event;

import java.util.List;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.util.DateUtil;

public record AlbumDeleteCacheEvent(Long albumId, String yearMonth, List<Long> userIds) implements AlbumChangeEvent {
    public static AlbumNameChangeEvent from(Album album) {
        return new AlbumNameChangeEvent(
                album.getId(),
                DateUtil.getYearMonth(album.getCreatedAt()),
                album.getUserAlbums().stream().map(ua -> ua.getUser().getId()).toList()
        );
    }
}
