package ongi.ongibe.global.eventlistener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.cache.album.AlbumCacheService;
import ongi.ongibe.cache.event.UserProfileUpdateCacheEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserProfileUpdateCacheEventListener {

    private final AlbumCacheService albumCacheService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserProfileUpdateCacheEvent(UserProfileUpdateCacheEvent event){
        log.info("유저정보 변경에 따른 캐시 rewrite, userId={}", event.userId());
        albumCacheService.refreshAllMonthlyAlbumCache(event.userId());
    }
}
