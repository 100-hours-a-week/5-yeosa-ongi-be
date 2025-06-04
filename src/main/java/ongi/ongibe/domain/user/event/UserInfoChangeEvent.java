package ongi.ongibe.domain.user.event;

import java.util.List;

public record UserInfoChangeEvent(List<Long> userIds, String yearMonth) {
}
