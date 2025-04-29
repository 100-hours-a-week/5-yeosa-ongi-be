package ongi.ongibe.domain.album.repository;

import ongi.ongibe.domain.album.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {
}
