package ongi.ongibe.domain.notification.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.album.UserAlbumRole;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.UserAlbum;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.domain.notification.NotificationType;
import ongi.ongibe.domain.notification.dto.NotificationListResponseDTO;
import ongi.ongibe.domain.notification.dto.NotificationResponseDTO;
import ongi.ongibe.domain.notification.entity.Notification;
import ongi.ongibe.domain.notification.event.AlbumCreatedNotificationEvent;
import ongi.ongibe.domain.notification.event.AlbumIdEvent;
import ongi.ongibe.domain.notification.event.InviteMemberNotificationEvent;
import ongi.ongibe.domain.notification.repository.NotificationRepository;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.security.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final UserAlbumRepository userAlbumRepository;
    private final SecurityUtil securityUtil;

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

    @Transactional
    public void saveInviteMemberNotification(
            InviteMemberNotificationEvent inviteMemberNotificationEvent) {
        Album album = getAlbum(inviteMemberNotificationEvent);
        User actor = getUser(inviteMemberNotificationEvent);
        log.info("notice user: {}, album: {}", actor.getId(), album.getId());
        List<UserAlbum> albumUser = userAlbumRepository.findAllByAlbum(album);
        List<Notification> notifications = albumUser.stream()
                .map(ua -> Notification.builder()
                        .user(ua.getUser())
                        .actorUser(actor)
                        .type(NotificationType.ALBUM_MEMBER_JOIN)
                        .refId(album.getId())
                        .isRead(false)
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
    }

    @Transactional(readOnly = true)
    public BaseApiResponse<NotificationListResponseDTO<NotificationResponseDTO>> getNotifications(
            Long lastNotificationId
    ) {
        int size = 10;
        Long userId = securityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Notification> page = notificationRepository.findUnreadByUserIdWithCursor(userId, lastNotificationId, pageable);

        List<NotificationResponseDTO> content = page.stream()
                .map(notification -> NotificationResponseDTO.from(notification, notification.getType().buildMessage(notification)))
                .toList();

        NotificationListResponseDTO<NotificationResponseDTO> response =
                NotificationListResponseDTO.from(new PageImpl<>(content, pageable, page.getTotalElements()));
        return BaseApiResponse.success("NOTIFICATION_LIST_SUCCESS", "알림 목록을 불러왔습니다.", response);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = getNotification(notificationId);
        notification.setRead(true);
    }

    private Album getAlbum(AlbumIdEvent event) {
        return albumRepository.findById(event.albumId())
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "앨범을 찾을 수 없습니다. albumId: " + event.albumId())
                );
    }

    private User getUser(AlbumIdEvent event) {
        return userRepository.findById(event.actorId())
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "유저를 찾을 수 없습니다. userId: " + event.actorId())
                );
    }

    private Notification getNotification(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "알림을를 찾을 수 없습니다. notificationId: " + notificationId)
                );
    }

    @Transactional
    public void albumAiCreated(Long albumId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "앨범을 찾을 수 없습니다."));

        List<UserAlbum> members = userAlbumRepository.findAllByAlbum(album);

        User actorUser = members.stream()
                .filter(member -> member.getRole() == UserAlbumRole.OWNER)
                .findFirst()
                .map(UserAlbum::getUser)
                .orElseThrow(() -> new IllegalStateException("OWNER 역할의 사용자가 없습니다."));

        List<Notification> notifications = members.stream()
                .map(member -> Notification.builder()
                        .user(member.getUser())
                        .actorUser(actorUser)
                        .type(NotificationType.ALBUM_CREATED_AI)
                        .refId(album.getId())
                        .isRead(false)
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
    }
}
