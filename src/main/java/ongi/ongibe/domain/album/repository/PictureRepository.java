package ongi.ongibe.domain.album.repository;

import java.time.LocalDateTime;
import java.util.List;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
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
    @Query("update Picture p set p.isShaky = true where p.s3Key in :keys and p.album.id = :albumId")
    int markPicturesAsShaky(@Param("albumId") Long albumId, @Param("keys") List<String> keys);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update Picture p set p.isShaky = false where p.s3Key in :keys and p.album.id = :albumId")
    void markPicturesShakyAsStable(@Param("albumId") Long albumId, @Param("keys") List<String> keys);

    @Modifying
    @Transactional
    @Query("update Picture p set p.isDuplicated = true where p.s3Key in :keys and p.album.id = :albumId")
    int markPicturesAsDuplicated(@Param("albumId") Long albumId, @Param("keys") List<String> keys);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update Picture p set p.isDuplicated = false where p.s3Key in :keys and p.album.id = :albumId")
    void markPicturesDuplicatedAsStable(@Param("albumId") Long albumId, @Param("keys") List<String> keys);

    @Modifying
    @Transactional
    @Query("update Picture p set p.tag = :tag where p.s3Key in :keys and p.tag is null and p.album.id = :albumId")
    int updateTagIfAbsent(@Param("albumId") Long albumId, @Param("keys") List<String> keys, @Param("tag") String tag);

    @Modifying
    @Transactional
    @Query("update Picture p set p.qualityScore = :score where p.s3Key in :keys")
    int updateScore(@Param("albumId") Long albumId, @Param("keys") String keys, @Param("score") Double score);


    @Query("""
        select DATE(p.createdAt), count(p)
        from Picture p
        where p.user.id = :userId
        and p.createdAt between :startDate and :endDate
        group by DATE(p.createdAt)
        order by DATE(p.createdAt)
    """)
    List<Object[]> countPicturesByDate(@Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("""
        select p.place.city, p.place.district, p.place.town
        from Picture p
        where p.user.id = :userId
            and p.createdAt between :start and :end
        group by p.place.city, p.place.district, p.place.town
        order by count(p) desc
    """)
    List<Object[]> mostVisitPlace(@Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);

    @Query("""
    select p
    from Picture p
    where p.user = :user
      and p.place.city = :city
      and p.place.district = :district
      and p.place.town = :town
      and p.createdAt between :start and :end
    """)
    List<Picture> findByUserAndPlaceAndCreatedAtBetween(@Param("user") User user,
            @Param("city") String city,
            @Param("district") String district,
            @Param("town") String town,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<Picture> findAllByUserAndCreatedAtBetween(User user, LocalDateTime createdAtAfter, LocalDateTime createdAtBefore);

    List<Picture> findAllByAlbumIdAndS3KeyIn(Long albumId, List<String> s3Keys);
}
