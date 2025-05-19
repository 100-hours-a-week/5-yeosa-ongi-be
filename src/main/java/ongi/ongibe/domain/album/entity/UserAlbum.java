package ongi.ongibe.domain.album.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ongi.ongibe.UserAlbumRole;
import ongi.ongibe.domain.user.entity.User;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@SQLDelete(sql = "update user_album set deleted_at = NOW() where id = ?")
@SQLRestriction("deleted_at IS NULL")
@Table(name = "user_album", indexes = {
        @Index(name = "idx_user", columnList = "user_id"),
        @Index(name = "idx_album", columnList = "album_id"),
})
public class UserAlbum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private UserAlbumRole role;

    private LocalDateTime deletedAt;

    public static UserAlbum of(User user, Album album, UserAlbumRole role) {
        return UserAlbum.builder()
                .user(user)
                .album(album)
                .role(role)
                .build();
    }
}
