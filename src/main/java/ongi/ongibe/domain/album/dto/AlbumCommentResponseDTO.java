package ongi.ongibe.domain.album.dto;

import java.time.LocalDateTime;
import ongi.ongibe.domain.album.entity.Comments;
import ongi.ongibe.domain.user.entity.User;

public record AlbumCommentResponseDTO(
        Long commentId,
        String userName,
        String userProfile,
        String content,
        LocalDateTime createdAt
) {
    public static AlbumCommentResponseDTO from(Comments comment) {
        User user = comment.getUser();
        return new AlbumCommentResponseDTO(
                comment.getId(),
                user.getNickname(),
                user.getProfileImage(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
