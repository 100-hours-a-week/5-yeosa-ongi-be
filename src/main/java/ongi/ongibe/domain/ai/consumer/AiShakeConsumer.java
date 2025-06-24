package ongi.ongibe.domain.ai.consumer;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.dto.AiEmbeddingResponseDTO;
import ongi.ongibe.domain.ai.dto.KafkaResponseDTOWrapper;
import ongi.ongibe.domain.ai.dto.ShakyResponseDTO;
import ongi.ongibe.domain.ai.kafka.AiStepTransitionService;
import ongi.ongibe.domain.ai.repository.AiTaskStatusRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiShakeConsumer extends AbstractAiConsumer<KafkaResponseDTOWrapper<ShakyResponseDTO>>{

    private final PictureRepository pictureRepository;

    public AiShakeConsumer(
            AiTaskStatusRepository taskStatusRepository, AiStepTransitionService transitionService, PictureRepository pictureRepository) {
        super(taskStatusRepository, transitionService);
        this.pictureRepository = pictureRepository;
    }

    @KafkaListener(
            topics = "#{'${kafka.topic.response.quality'}",
            containerFactory = "batchKafkaListenerContainerFactory"
    )
    public void consume(List<KafkaResponseDTOWrapper<ShakyResponseDTO>> responses) {
        for (KafkaResponseDTOWrapper<ShakyResponseDTO> res : responses) {
            this.consume(res);
            if  (res.statusCode() == 201) {
                Long albumId = res.albumId();
                List<String> shakyKeys = res.body().data();
                pictureRepository.markPicturesAsShaky(albumId, shakyKeys);
                log.info("[DUPLICATE] album {} 중복 {}장 처리 완료", albumId, shakyKeys.size());
            }
        }
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
