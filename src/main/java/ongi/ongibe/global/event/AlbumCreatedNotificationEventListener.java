package ongi.ongibe.global.event;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.event.AlbumAiCreateNotificationEvent;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.UserAlbum;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.domain.notification.NotificationService;
import ongi.ongibe.domain.notification.NotificationType;
import ongi.ongibe.domain.notification.entity.Notification;
import ongi.ongibe.domain.notification.event.AlbumCreatedNotificationEvent;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.server.ResponseStatusException;

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
        notificationService.albumAiCreated(event.albumId(), event.actorId());
    }
}
