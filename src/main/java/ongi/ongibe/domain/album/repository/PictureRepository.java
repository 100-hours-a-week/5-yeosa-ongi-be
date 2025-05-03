package ongi.ongibe.domain.album.repository;

import java.util.Collection;
import java.util.List;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO.PictureCoordinate;
import ongi.ongibe.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PictureRepository extends JpaRepository<Picture, Long> {

    List<Picture> findAllByUser(User user);

    List<Picture> findAllByPictureURLIn(List<String> pictureURLS);

    @Modifying
    @Transactional
    @Query("update Picture p set p.isShaky = true where p.pictureURL in :urls")
    int markPicturesAsShaky(@Param("urls") List<String> urls);

    @Modifying
    @Transactional
    @Query("update Picture p set p.isShaky = false where p.pictureURL in :urls")
    int markPicturesShakyAsStable(@Param("urls") List<String> urls);

    @Modifying
    @Transactional
    @Query("update Picture p set p.isDuplicated = true where p.pictureURL in :url")
    int markPicturesAsDuplicated(@Param("urls") List<String> urls);

    @Modifying
    @Transactional
    @Query("update Picture p set p.isDuplicated = false where p.pictureURL in :urls")
    int markPicturesDuplicatedAsStable(@Param("urls") List<String> urls);

    @Modifying
    @Transactional
    @Query("""
            update Picture p
            set p.tag = :tag, p.qualityScore = :score 
            where p.pictureURL in :url
    """)
    int updateScoreAndTagIfAbsent(@Param("url") String url,
            @Param("score") Double score,
            @Param("tag") String tag);

}
