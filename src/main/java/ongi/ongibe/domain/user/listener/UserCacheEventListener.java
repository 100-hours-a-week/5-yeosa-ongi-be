package ongi.ongibe.domain.user.listener;

import lombok.RequiredArgsConstructor;
import ongi.ongibe.domain.user.event.UserInfoChangeEvent;
import ongi.ongibe.global.cache.CacheKeyUtil;
import ongi.ongibe.global.cache.RedisCacheService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserCacheEventListener {

    private final RedisCacheService cacheService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserChange(UserInfoChangeEvent event) {
        String yearMonth = event.yearMonth();
        for (Long userId : event.userIds()) {
            String key = CacheKeyUtil.key("monthlyAlbum", userId, yearMonth);
            cacheService.evict(key);
        }
    }
}
