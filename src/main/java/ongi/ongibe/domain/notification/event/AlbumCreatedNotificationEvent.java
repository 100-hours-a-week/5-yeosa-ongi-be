package ongi.ongibe.domain.notification.event;

public record AlbumCreatedNotificationEvent(
        Long albumId,
        Long actorId
) {}

