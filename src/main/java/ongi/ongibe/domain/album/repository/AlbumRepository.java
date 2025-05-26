package ongi.ongibe.domain.album.repository;

import java.util.Collection;
import java.util.List;
import ongi.ongibe.domain.album.AlbumProcessState;
import ongi.ongibe.domain.album.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    List<Album> findByProcessStateIn(List<AlbumProcessState> processStates);
}
