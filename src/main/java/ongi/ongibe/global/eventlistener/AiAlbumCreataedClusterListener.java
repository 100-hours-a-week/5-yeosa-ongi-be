package ongi.ongibe.global.eventlistener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.album.event.AlbumClusterEvent;
import ongi.ongibe.domain.album.service.AlbumProcessService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiAlbumCreataedClusterListener {

    private final AlbumProcessService albumProcessService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handledAlbumCreatedCluster(AlbumClusterEvent event) {
        log.info("cluster event received: {}", event.albumId());
        albumProcessService.processAlbumAsync(event.albumId(), event.pictureS3Keys());
    }
}
