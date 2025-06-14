package ongi.ongibe.domain.album.repository;

import java.time.LocalDateTime;
import java.util.List;
import ongi.ongibe.domain.album.entity.FaceCluster;
import ongi.ongibe.domain.album.entity.PictureFaceCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PictureFaceClusterRepository extends JpaRepository<PictureFaceCluster, Long> {

    List<PictureFaceCluster> findAllByFaceCluster(FaceCluster faceCluster);

    @Modifying(clearAutomatically = true)
    @Query("""
    update PictureFaceCluster pfc set pfc.deletedAt = :now where pfc.picture.id in :pictureIds
    """)
    void deleteAllByPictureIds(@Param("now")LocalDateTime now, @Param("pictureIds") List<Long> pictureIds);

    @Modifying(clearAutomatically = true)
    @Query("""
    update PictureFaceCluster pfc set pfc.deletedAt = :now where pfc.faceCluster.id in :faceClusterIds
    """)
    void deleteAllByFaceClusterIds(@Param("now")LocalDateTime now, @Param("faceClusterIds") List<Long> faceClusterIds);
}
