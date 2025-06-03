package ongi.ongibe.domain.album.listener;

import lombok.RequiredArgsConstructor;
import ongi.ongibe.domain.album.event.AlbumChangeEvent;
import ongi.ongibe.global.cache.CacheKeyUtil;
import ongi.ongibe.global.cache.RedisCacheService;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AlbumChangeCacheEventListener {

    private final RedisCacheService cacheService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAlbumChange(AlbumChangeEvent event) {
        for (Long userId : event.userIds()) {
            String key = CacheKeyUtil.key("monthlyAlbum", userId, event.yearMonth());
            cacheService.evict(key);
        }
    }
}