package ongi.ongibe.domain.album.repository;

import java.util.List;
import ongi.ongibe.domain.album.entity.FaceCluster;
import ongi.ongibe.domain.album.entity.PictureFaceCluster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PictureFaceClusterRepository extends JpaRepository<PictureFaceCluster, Long> {

    List<PictureFaceCluster> findAllByFaceCluster(FaceCluster faceCluster);
}
