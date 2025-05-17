package ongi.ongibe.domain.notification;

import ongi.ongibe.domain.notification.entity.Notification;

public enum NotificationType {

    ALBUM_CREATED {
        @Override
        public String buildMessage(Notification notification) {
            return "앨범이 생성되었습니다.";
        }
    },
    ALBUM_CREATED_AI {
        @Override
        public String buildMessage(Notification notification) {
            return "앨범 분류가 완료되었습니다. 확인해보세요!";
        }
    },
    FEED_COMMENT {
        @Override
        public String buildMessage(Notification notification) {
            return notification.getActorUser().getNickname() + "님이 댓글을 남겼습니다.";
        }
    },
    FEED_LIKE {
        @Override
        public String buildMessage(Notification notification) {
            return notification.getActorUser().getNickname() + "님이 좋아요를 눌렀습니다.";
        }
    },
    FOLLOW {
        @Override
        public String buildMessage(Notification notification) {
            return notification.getActorUser().getNickname() + "님이 회원님을 팔로우했습니다.";
        }
    };

    public abstract String buildMessage(Notification notification);
}
