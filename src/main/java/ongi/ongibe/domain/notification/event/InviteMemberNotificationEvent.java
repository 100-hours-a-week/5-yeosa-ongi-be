package ongi.ongibe.domain.notification.event;

public record InviteMemberNotificationEvent(
        Long albumId,
        Long actorId
) implements AlbumIdEvent {}
