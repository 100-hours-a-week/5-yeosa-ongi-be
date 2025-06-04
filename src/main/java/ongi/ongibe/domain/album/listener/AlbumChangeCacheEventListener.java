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
        String yearMonth = event.yearMonth();
        for (Long userId : event.userIds()) {
            String AlbumKey = CacheKeyUtil.key("monthlyAlbum", userId, yearMonth);
            String UserTotalStateKey = CacheKeyUtil.key("userTotalStat", userId);
            String UserTagStateKey = CacheKeyUtil.key("userTagStat", userId, yearMonth);
            cacheService.evict(AlbumKey);
            cacheService.evict(UserTotalStateKey);
            cacheService.evict(UserTagStateKey);
        }
    }
}