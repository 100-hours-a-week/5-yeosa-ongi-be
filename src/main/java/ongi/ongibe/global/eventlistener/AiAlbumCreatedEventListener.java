package ongi.ongibe.global.eventlistener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.album.event.AlbumEvent;
import ongi.ongibe.domain.album.service.AlbumProcessTransactionService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiAlbumCreatedEventListener {

    private final AlbumProcessTransactionService albumProcessTransactionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handledAlbumCreated(AlbumEvent event) {
        log.info("event received: {}", event.albumId());
        albumProcessTransactionService.processAlbumTransaction(event.albumId(), event.userId(), event.pictureS3Keys(), event.concepts());
    }
}
