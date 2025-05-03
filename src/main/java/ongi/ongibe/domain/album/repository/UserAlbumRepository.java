package ongi.ongibe.domain.album.repository;

import java.time.LocalDateTime;
import java.util.List;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.UserAlbum;
import ongi.ongibe.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAlbumRepository extends JpaRepository<UserAlbum, Long> {

    List<UserAlbum> findAllByUser(User user);
    boolean existsByUserAndAlbum_CreatedAtBefore(User user, LocalDateTime dateTime);

    int countByUser(User user);

    UserAlbum findByUserAndAlbum(User user, Album album);
}
