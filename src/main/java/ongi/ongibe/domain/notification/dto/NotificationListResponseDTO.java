package ongi.ongibe.domain.notification.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record NotificationListResponseDTO<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> NotificationListResponseDTO<T> from(Page<T> pageData) {
        return new NotificationListResponseDTO<>(
                pageData.getContent(),
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages(),
                pageData.isLast()
        );
    }
}
