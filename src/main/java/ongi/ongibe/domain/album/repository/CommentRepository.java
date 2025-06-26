package ongi.ongibe.domain.album.repository;

import ongi.ongibe.domain.album.entity.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comments, Long> {

}
