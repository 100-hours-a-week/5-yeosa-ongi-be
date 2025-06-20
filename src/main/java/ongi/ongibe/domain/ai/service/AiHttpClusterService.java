package ongi.ongibe.domain.ai.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.aiInterface.AiClusterServiceInterface;
import ongi.ongibe.domain.ai.dto.AiClusterResponseDTO.ClusterData;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.FaceCluster;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.entity.PictureFaceCluster;
import ongi.ongibe.domain.album.repository.FaceClusterRepository;
import ongi.ongibe.domain.album.repository.PictureFaceClusterRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiHttpClusterService implements AiClusterServiceInterface {

    private final AiClient aiClient;
    private final PictureRepository pictureRepository;
    private final FaceClusterRepository faceClusterRepository;
    private final PictureFaceClusterRepository pictureFaceClusterRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requestCluster(Long albumId, Long userId, List<String> s3keys) {
        log.info("[AI] 클러스터 시작");
        List<Long> faceClusterIds = faceClusterRepository.findAllByAlbumId(albumId).stream()
                .map(FaceCluster::getId)
                .toList();
        if (!faceClusterIds.isEmpty()) {
            pictureFaceClusterRepository.deleteAllByFaceClusterIds(LocalDateTime.now(), faceClusterIds);
            faceClusterRepository.deleteAllByIdInBatch(faceClusterIds);
            log.info("[AI] 기존 클러스터 및 매핑 삭제 완료 (총 {}개)", faceClusterIds.size());
        }

        List<ClusterData> clusters =
                aiClient.getClusters(albumId, pictureRepository.findAllByAlbumId(albumId).stream()
                        .map(Picture::getS3Key).toList());

        Map<String, Picture> pictureMap = pictureRepository.findAllByAlbumId(albumId).stream()
                .collect(Collectors.toMap(Picture::getS3Key, p -> p));
        int clusterIndex = 1;

        for (var cluster : clusters) {
            var repKey = cluster.representativeFace().image();
            Picture repPic = pictureMap.get(repKey);

            var bbox = cluster.representativeFace().bbox();
            FaceCluster faceCluster = FaceCluster.builder()
                    .representativePicture(repPic)
                    .clusterName("사람-" + clusterIndex++)
                    .bboxX1(bbox.get(0))
                    .bboxY1(bbox.get(1))
                    .bboxX2(bbox.get(2))
                    .bboxY2(bbox.get(3))
                    .build();
            faceClusterRepository.save(faceCluster);

            var mappings = cluster.images().stream()
                    .map(pictureMap::get)
                    .filter(Objects::nonNull)
                    .map(pic -> PictureFaceCluster.builder().picture(pic).faceCluster(faceCluster)
                            .build())
                    .toList();
            pictureFaceClusterRepository.saveAll(mappings);
            log.info("[AI] 클러스터 완료");
        }
    }
}