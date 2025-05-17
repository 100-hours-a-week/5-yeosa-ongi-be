package ongi.ongibe.domain.notification.dto;

import java.time.LocalDateTime;
import ongi.ongibe.domain.notification.NotificationType;
import ongi.ongibe.domain.notification.entity.Notification;

public record NotificationResponseDTO(
        Long notificationId,
        NotificationType type,
        Long resourceId,
        String message,
        LocalDateTime createdAt
) {
    public static NotificationResponseDTO from(Notification notification, String message) {
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getType(),
                notification.getRefId(),
                message,
                notification.getCreatedAt()
        );
    }
}
