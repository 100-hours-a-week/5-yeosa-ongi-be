package ongi.ongibe.domain.ai.consumer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.dto.AiClusterResponseDTO;
import ongi.ongibe.domain.ai.dto.AiClusterResponseDTO.ClusterData;
import ongi.ongibe.domain.ai.dto.KafkaResponseDTOWrapper;
import ongi.ongibe.domain.ai.kafka.AiStepTransitionService;
import ongi.ongibe.domain.ai.repository.AiTaskStatusRepository;
import ongi.ongibe.domain.album.entity.FaceCluster;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.entity.PictureFaceCluster;
import ongi.ongibe.domain.album.repository.FaceClusterRepository;
import ongi.ongibe.domain.album.repository.PictureFaceClusterRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiClusterConsumer extends AbstractAiConsumer<KafkaResponseDTOWrapper<AiClusterResponseDTO>> {

    private final PictureRepository pictureRepository;
    private final FaceClusterRepository faceClusterRepository;
    private final PictureFaceClusterRepository pictureFaceClusterRepository;

    public AiClusterConsumer(AiTaskStatusRepository aiTaskStatusRepository, AiStepTransitionService transitionService, PictureRepository pictureRepository, FaceClusterRepository faceClusterRepository, PictureFaceClusterRepository pictureFaceClusterRepository) {
        super(aiTaskStatusRepository, transitionService);
        this.pictureRepository = pictureRepository;
        this.faceClusterRepository = faceClusterRepository;
        this.pictureFaceClusterRepository = pictureFaceClusterRepository;
    }

    @KafkaListener(
            topics = "${kafka.topic.response.people}",
            containerFactory = "clusterKafkaListenerContainerFactory"
    )
    public void consume(List<KafkaResponseDTOWrapper<AiClusterResponseDTO>> responses) {
        for (KafkaResponseDTOWrapper<AiClusterResponseDTO> response : responses) {
            this.consume(response);
            if (response.statusCode() == 201) {
                Long albumId = response.albumId();
                List<Long> faceClusterIds = faceClusterRepository.findAllByAlbumId(albumId).stream()
                        .map(FaceCluster::getId)
                        .toList();
                if (!faceClusterIds.isEmpty()) {
                    pictureFaceClusterRepository.deleteAllByFaceClusterIds(LocalDateTime.now(),
                            faceClusterIds);
                    faceClusterRepository.deleteAllByIdInBatch(faceClusterIds);
                    log.info("[AI] 기존 클러스터 및 매핑 삭제 완료 (총 {}개)", faceClusterIds.size());
                }

                List<ClusterData> clusterData = response.body().data();

                Map<String, Picture> pictureMap = pictureRepository.findAllByAlbumId(albumId)
                        .stream()
                        .collect(Collectors.toMap(Picture::getS3Key, p -> p));
                int clusterIndex = 1;
                for (var cluster : clusterData) {
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
                            .map(pic -> PictureFaceCluster.builder().picture(pic)
                                    .faceCluster(faceCluster)
                                    .build())
                            .toList();
                    pictureFaceClusterRepository.saveAll(mappings);
                    log.info("[AI] 클러스터 완료");
                }
            }
        }
    }


    @Override
    protected int extractStatusCode(KafkaResponseDTOWrapper<AiClusterResponseDTO> response) {
        return response.statusCode();
    }

    @Override
    protected String extractTaskId(KafkaResponseDTOWrapper<AiClusterResponseDTO> response) {
        return response.taskId();
    }

    @Override
    protected String extractMessage(KafkaResponseDTOWrapper<AiClusterResponseDTO> response) {
        return response.body().message();
    }

    @Override
    protected String extractErrorData(KafkaResponseDTOWrapper<AiClusterResponseDTO> response) {
        return response.body().data().isEmpty() ? "" : response.body().data().toString();
    }

    @Override
    protected AiStep getStep() {
        return AiStep.PEOPLE;
    }
}
