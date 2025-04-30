package ongi.ongibe.domain.album.repository;

import java.util.List;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO.PictureCoordinate;
import ongi.ongibe.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PictureRepository extends JpaRepository<Picture, Long> {

    List<PictureCoordinate> findAllByUser(User user);
}
