package ongi.ongibe.domain.ai.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.dto.KafkaResponseDTOWrapper;
import ongi.ongibe.domain.ai.dto.ShakyResponseDTO;
import ongi.ongibe.domain.ai.kafka.AiStepTransitionService;
import ongi.ongibe.domain.ai.producer.AiEmbeddingProducer;
import ongi.ongibe.domain.ai.repository.AiTaskStatusRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.album.service.AlbumMarkService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiShakeConsumer extends AbstractAiConsumer<KafkaResponseDTOWrapper<ShakyResponseDTO>>{

    private final PictureRepository pictureRepository;

    public AiShakeConsumer(
            AiTaskStatusRepository taskStatusRepository, AiStepTransitionService transitionService, AlbumMarkService albumMarkService, ObjectMapper objectMapper, AiEmbeddingProducer embeddingProducer,PictureRepository pictureRepository) {
        super(taskStatusRepository, transitionService, albumMarkService, objectMapper, embeddingProducer);
        this.pictureRepository = pictureRepository;
    }

    @KafkaListener(
            topics = "${kafka.topic.response.quality}",
            containerFactory = "genericKafkaListenerContainerFactory"
    )
    public void consume(List<KafkaResponseDTOWrapper<ShakyResponseDTO>> responses) {
        for (KafkaResponseDTOWrapper<ShakyResponseDTO> res : responses) {
            if  (res.statusCode() == 201) {
                ShakyResponseDTO dto = objectMapper.convertValue(res.body(), ShakyResponseDTO.class);
                Long albumId = res.albumId();
                List<String> shakyKeys = dto.data();
                pictureRepository.markPicturesAsShaky(albumId, shakyKeys);
                log.info("[QUALITY] album {} 사진 흔들림 {}장 처리 완료", albumId, shakyKeys.size());
            }
            this.consume(res);
        }
    }

    @Override
    protected Long extractAlbumId(KafkaResponseDTOWrapper<ShakyResponseDTO> response) {
        return response.albumId();
    }

    @Override
    protected int extractStatusCode(KafkaResponseDTOWrapper<ShakyResponseDTO> response) {
        return response.statusCode();
    }

    @Override
    protected String extractTaskId(KafkaResponseDTOWrapper<ShakyResponseDTO> response) {
        return response.taskId();
    }

    @Override
    protected String extractMessage(KafkaResponseDTOWrapper<ShakyResponseDTO> response) {
        return response.body().message();
    }

    @Override
    protected String extractErrorData(KafkaResponseDTOWrapper<ShakyResponseDTO> response) {
        return response.body().data().isEmpty() ? "" : String.join(", ", response.body().data());
    }

    @Override
    protected AiStep getStep() {
        return AiStep.QUALITY;
    }
}
