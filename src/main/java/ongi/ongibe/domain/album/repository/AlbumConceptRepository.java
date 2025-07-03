package ongi.ongibe.domain.album.repository;

import java.util.List;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.AlbumConcept;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumConceptRepository extends JpaRepository<AlbumConcept, Long> {

    List<AlbumConcept> findAllByAlbum(Album album);

    List<AlbumConcept> findAllByAlbumId(Long albumId);
}
