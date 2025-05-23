package ongi.ongibe.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.notification.service.NotificationService;
import ongi.ongibe.domain.notification.dto.NotificationListResponseDTO;
import ongi.ongibe.domain.notification.dto.NotificationResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<BaseApiResponse<NotificationListResponseDTO<NotificationResponseDTO>>> getNotifications(
            @RequestParam(required = false) Long lastNotificationId
    ) {
        BaseApiResponse<NotificationListResponseDTO<NotificationResponseDTO>> response =
                notificationService.getNotifications(lastNotificationId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{notificationId}")
    public ResponseEntity<BaseApiResponse<Void>> readNotification(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(BaseApiResponse.success("NOTIFICATION_READ_SUCCESS", "알림 읽기 성공했습니다.", null));
    }
}
