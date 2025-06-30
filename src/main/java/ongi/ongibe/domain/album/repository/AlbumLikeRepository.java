package ongi.ongibe.domain.album.repository;

import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.AlbumLike;
import ongi.ongibe.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumLikeRepository extends JpaRepository<AlbumLike, Long> {

    int countByAlbumId(Long albumId);

    @Modifying
    @Query(value = """
    INSERT IGNORE INTO album_likes (album_id, user_id)
    VALUES (:albumId, :userId)
""", nativeQuery = true)
    void upsert(@Param("albumId") Long albumId, @Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM album_likes WHERE album_id = :albumId AND user_id = :userId", nativeQuery = true)
    void deleteByAlbumIdAndUserId(@Param("albumId") Long albumId, @Param("userId") Long userId);

}
