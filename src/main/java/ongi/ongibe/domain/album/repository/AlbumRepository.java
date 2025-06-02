package ongi.ongibe.domain.album.repository;

import java.util.Collection;
import java.util.List;
import ongi.ongibe.domain.album.AlbumProcessState;
import ongi.ongibe.domain.album.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    @Query("""
    SELECT DISTINCT a FROM Album a
    LEFT JOIN FETCH a.userAlbums ua
    LEFT JOIN FETCH ua.user
    WHERE a.processState IN :states
""")
    List<Album> findByProcessStateIn(List<AlbumProcessState> processStates);
}
