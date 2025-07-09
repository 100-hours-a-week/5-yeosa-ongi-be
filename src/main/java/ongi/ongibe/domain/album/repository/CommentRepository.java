package ongi.ongibe.domain.album.repository;

import java.time.LocalDateTime;
import java.util.List;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comments, Long> {

    List<Comments> findAllByAlbum(Album album);

    @Modifying
    @Query("""
    update Comments comments set comments.deletedAt = :now where comments.id = :commentsId
    """)
    void deleteById(@Param("commentsId") Long commentsId, @Param("now")LocalDateTime now);

    int countAllByAlbum(Album album);
}
