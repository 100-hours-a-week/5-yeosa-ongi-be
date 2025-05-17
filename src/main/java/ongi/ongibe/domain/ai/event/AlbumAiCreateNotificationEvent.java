package ongi.ongibe.domain.ai.event;

public record AlbumAiCreateNotificationEvent(
        Long albumId,
        Long actorId
) {}
