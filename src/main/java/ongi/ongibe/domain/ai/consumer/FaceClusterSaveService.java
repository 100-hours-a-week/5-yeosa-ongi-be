package ongi.ongibe.domain.ai.consumer;

import java.util.List;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.domain.album.entity.FaceCluster;
import ongi.ongibe.domain.album.entity.PictureFaceCluster;
import ongi.ongibe.domain.album.repository.FaceClusterRepository;
import ongi.ongibe.domain.album.repository.PictureFaceClusterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FaceClusterSaveService {

    private final FaceClusterRepository faceClusterRepository;
    private final PictureFaceClusterRepository pictureFaceClusterRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAll(List<FaceCluster> clusters, List<PictureFaceCluster> mappings) {
        faceClusterRepository.saveAll(clusters);
        pictureFaceClusterRepository.saveAll(mappings);
    }
}
