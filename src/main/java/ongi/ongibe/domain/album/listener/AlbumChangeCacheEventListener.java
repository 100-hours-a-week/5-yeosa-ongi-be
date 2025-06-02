package ongi.ongibe.domain.album.listener;

import lombok.RequiredArgsConstructor;
import ongi.ongibe.domain.album.event.AlbumChangeEvent;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AlbumChangeCacheEventListener {

    private final CacheManager cacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAlbumChange(AlbumChangeEvent event) {
        for (Long userId : event.userIds()) {
            String key = userId + ":" + event.yearMonth();
            cacheManager.getCache("monthlyAlbum").evict(key);
        }
    }
}