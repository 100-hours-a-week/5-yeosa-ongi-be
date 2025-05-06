package ongi.ongibe.domain.album.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update Picture p set p.isShaky = false where p.pictureURL in :urls")
    int markPicturesShakyAsStable(@Param("urls") List<String> urls);

    @Modifying
    @Transactional
    @Query("update Picture p set p.isDuplicated = true where p.pictureURL in :urls")
    int markPicturesAsDuplicated(@Param("urls") List<String> urls);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update Picture p set p.isDuplicated = false where p.pictureURL in :urls")
    int markPicturesDuplicatedAsStable(@Param("urls") List<String> urls);

    @Modifying
    @Transactional
    @Query("update Picture p set p.tag = :tag where p.pictureURL in :urls and p.tag is null")
    int updateTagIfAbsent(@Param("urls") List<String> urls, @Param("tag") String tag);

    @Modifying
    @Transactional
    @Query("update Picture p set p.qualityScore = :score where p.pictureURL in :url")
    int updateScore(@Param("url") String url, @Param("score") Double score);


    @Query("""
        select DATE(p.createdAt), count(p)
        from Picture p
        where p.user.id = :userId
        and p.createdAt between :startDate and :endDate
        group by DATE(p.createdAt)
        order by DATE(p.createdAt)
    """)
    List<Object[]> countPicturesByDate(@Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
