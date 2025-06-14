package ongi.ongibe.domain.album.repository;

import java.util.List;
import ongi.ongibe.domain.album.entity.FaceCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FaceClusterRepository extends JpaRepository<FaceCluster, Long> {

    List<FaceCluster> findAllByRepresentativePicture_Album_Id(Long representativePictureAlbumId);

    @Query("SELECT fc.representativePicture.id FROM FaceCluster fc")
    List<Long> findAllRepresentativePictureIds();

    @Query("SELECT fc FROM FaceCluster fc WHERE fc.representativePicture.album.id = :albumId")
    List<FaceCluster> findAllByAlbumId(@Param("albumId") Long albumId);
}
