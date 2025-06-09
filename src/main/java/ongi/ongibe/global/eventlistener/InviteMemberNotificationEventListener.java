package ongi.ongibe.global.eventlistener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.notification.service.NotificationService;
import ongi.ongibe.domain.notification.event.InviteMemberNotificationEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class InviteMemberNotificationEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInviteMemberNotification(InviteMemberNotificationEvent event) {
        log.info("invite notification event received: {}", event.albumId());
        notificationService.saveInviteMemberNotification(event);
    }
}
