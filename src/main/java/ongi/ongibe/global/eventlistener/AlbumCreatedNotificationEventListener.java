package ongi.ongibe.global.eventlistener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.event.AlbumAiCreateNotificationEvent;
import ongi.ongibe.domain.notification.service.NotificationService;
import ongi.ongibe.domain.notification.event.AlbumCreatedNotificationEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlbumCreatedNotificationEventListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAlbumCreated(AlbumCreatedNotificationEvent event) {
        log.info("notification event received: {}", event.albumId());
        notificationService.saveAlbumNotification(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAlbumAiCreated(AlbumAiCreateNotificationEvent event) {
        log.info("AI notification event received: {}", event.albumId());
        notificationService.albumAiCreated(event.albumId());
    }
}
