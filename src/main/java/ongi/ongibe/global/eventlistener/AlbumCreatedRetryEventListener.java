package ongi.ongibe.global.eventlistener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.album.event.AlbumRetryEvent;
import ongi.ongibe.domain.album.service.AlbumProcessTransactionService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlbumCreatedRetryEventListener {

    private final AlbumProcessTransactionService albumProcessTransactionService;

    @Async
    @EventListener
    public void handledAlbumCreatedRetry(AlbumRetryEvent event) {
        log.info("retry event received: {}", event.albumId());
        albumProcessTransactionService.processAlbumTransaction(event.albumId(), event.userId(), event.pictureS3Keys());
    }
}
