package ongi.ongibe.domain.album.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import ongi.ongibe.domain.album.AlbumProcessState;
import ongi.ongibe.domain.album.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    List<Album> findByProcessStateIn(List<AlbumProcessState> processStates);

    Optional<Album> findByName(String name);

    @Modifying
    @Query("UPDATE Album a SET a.likeCount = :likeCount WHERE a.id = :albumId")
    void updateLikeCount(@Param("albumId") Long albumId, @Param("likeCount") int likeCount);
}
