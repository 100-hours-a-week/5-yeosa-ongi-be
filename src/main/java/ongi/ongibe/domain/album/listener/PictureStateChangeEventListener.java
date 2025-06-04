package ongi.ongibe.domain.album.listener;

import lombok.RequiredArgsConstructor;
import ongi.ongibe.domain.album.event.PictureStatChangeEvent;
import ongi.ongibe.global.cache.CacheKeyUtil;
import ongi.ongibe.global.cache.RedisCacheService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PictureStateChangeEventListener {

    private final RedisCacheService redisCacheService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent(PictureStatChangeEvent event) {
        String yearMonth = event.yearMonth();
        for (Long userId: event.memberId()){
            String UserPictureStateKey = CacheKeyUtil.key("userPictureStat", userId, yearMonth);
            String UserPlaceStatKey = CacheKeyUtil.key("userPlaceStat", userId, yearMonth);
            redisCacheService.evict(UserPictureStateKey);
            redisCacheService.evict(UserPlaceStatKey);
        }
    }
}
