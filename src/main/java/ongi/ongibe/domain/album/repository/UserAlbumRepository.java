package ongi.ongibe.domain.album.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.UserAlbum;
import ongi.ongibe.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAlbumRepository extends JpaRepository<UserAlbum, Long> {

    @Query("""
    SELECT DISTINCT ua
    FROM UserAlbum ua
    JOIN FETCH ua.album a
    JOIN FETCH a.thumbnailPicture tp
    JOIN FETCH a.userAlbums aua
    JOIN FETCH aua.user u
    WHERE ua.user = :user AND ua.deletedAt IS NULL
        AND ua.deletedAt IS NULL AND a.deletedAt IS NULL
    """)
    List<UserAlbum> findAllByUser(@Param("user") User user);
    boolean existsByUserAndAlbum_CreatedAtBefore(User user, LocalDateTime dateTime);

    int countByUser(User user);

    Optional<UserAlbum> findByUserAndAlbum(User user, Album album);

    List<UserAlbum> findAllByAlbum(Album album);

    List<UserAlbum> findAllByAlbumAndUser(Album album, User user);

    @Query("""
    select distinct function('DATE_FORMAT', a.createdAt, '%Y-%m') 
    from UserAlbum ua
    join ua.album a
    where ua.user.id = :userId and ua.deletedAt is null and a.deletedAt is null
    """)
    List<String> findAllYearMonthsByUserId(@Param("userId") Long userId);

}
