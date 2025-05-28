package ongi.ongibe.domain.album.repository;

import java.util.List;
import ongi.ongibe.domain.album.entity.FaceCluster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaceClusterRepository extends JpaRepository<FaceCluster, Long> {

    List<FaceCluster> findAllByRepresentativePicture_Album_Id(Long representativePictureAlbumId);
}
