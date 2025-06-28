package ongi.ongibe.domain.album.repository;

import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.AlbumLike;
import ongi.ongibe.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumLikeRepository extends JpaRepository<AlbumLike, Long> {

    boolean existsbyAlbumAndUser(Album album, User user);

    int countByAlbumId(Long albumId);

    void deleteByAlbumAndUser(Album album, User user);
}
