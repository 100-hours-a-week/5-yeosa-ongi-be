package ongi.ongibe.domain.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.notification.entity.Notification;
import ongi.ongibe.domain.notification.event.AlbumCreatedNotificationEvent;
import ongi.ongibe.domain.notification.repository.NotificationRepository;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public void saveAlbumNotification(AlbumCreatedNotificationEvent createdNotificationEvent) {
        Album album = getAlbum(createdNotificationEvent);
        User user = getUser(createdNotificationEvent);
        log.info("notice user: {}, album: {}", user.getId(), album.getId());
        
        Notification notification = Notification.builder()
                .user(user)
                .actorUser(user)
                .type(NotificationType.ALBUM_CREATED)
                .refId(album.getId())
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    private Album getAlbum(AlbumCreatedNotificationEvent createdNotificationEvent) {
        return albumRepository.findById(createdNotificationEvent.albumId())
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "앨범을 찾을 수 없습니다. albumId: " + createdNotificationEvent.albumId())
                );
    }

    private User getUser(AlbumCreatedNotificationEvent createdNotificationEvent) {
        return userRepository.findById(createdNotificationEvent.actorId())
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "유저를 찾을 수 없습니다. userId: " + createdNotificationEvent.actorId())
                );
    }
}
