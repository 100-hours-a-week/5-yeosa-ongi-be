package ongi.ongibe.domain.album.repository;

import ongi.ongibe.domain.album.entity.Place;
import ongi.ongibe.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query("select count(distinct p.place) from Picture p where p.user = :user")
    int countDistinctByPicturesByUser(User user);

}
