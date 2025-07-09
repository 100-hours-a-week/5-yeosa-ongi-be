package ongi.ongibe.domain.ai.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.dto.DuplicateResponseDTO;
import ongi.ongibe.domain.ai.dto.KafkaResponseDTOWrapper;
import ongi.ongibe.domain.ai.kafka.AiStepTransitionService;
import ongi.ongibe.domain.ai.producer.AiEmbeddingProducer;
import ongi.ongibe.domain.ai.repository.AiTaskStatusRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.album.service.AlbumMarkService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiDuplicateConsumer extends AbstractAiConsumer<KafkaResponseDTOWrapper<DuplicateResponseDTO>> {

    private final PictureRepository pictureRepository;

    public AiDuplicateConsumer(AiTaskStatusRepository aiTaskStatusRepository, AiStepTransitionService transitionService, AlbumMarkService albumMarkService, ObjectMapper objectMapper,
            AiEmbeddingProducer embeddingProducer,
            PictureRepository pictureRepository) {
        super(aiTaskStatusRepository, transitionService, albumMarkService, objectMapper, embeddingProducer);
        this.pictureRepository = pictureRepository;
    }

    @KafkaListener(
            topics = "${kafka.topic.response.duplicate}",
            containerFactory = "genericKafkaListenerContainerFactory"
    )
    public void consume(List<KafkaResponseDTOWrapper<DuplicateResponseDTO>> responses) {
        for(KafkaResponseDTOWrapper<DuplicateResponseDTO> response : responses) {
            if (response.statusCode() == 201) {
                Long albumId = response.albumId();
                DuplicateResponseDTO dto = objectMapper.convertValue(response.body(), DuplicateResponseDTO.class);
                List<String> duplicateKeys = dto.data().stream()
                        .flatMap(List::stream)
                        .toList();
                pictureRepository.markPicturesAsDuplicated(albumId, duplicateKeys);
                log.info("[DUPLICATE] album {} 중복 {}장 처리 완료", albumId, duplicateKeys.size());
            }
            this.consume(response);
        }
    }

    @Override
    protected Long extractAlbumId(KafkaResponseDTOWrapper<DuplicateResponseDTO> response) {
        return response.albumId();
    }

    @Override
    protected int extractStatusCode(KafkaResponseDTOWrapper<DuplicateResponseDTO> response) {
        return response.statusCode();
    }

    @Override
    protected String extractTaskId(KafkaResponseDTOWrapper<DuplicateResponseDTO> response) {
        return response.taskId();
    }

    @Override
    protected String extractMessage(KafkaResponseDTOWrapper<DuplicateResponseDTO> response) {
        return response.body().message();
    }

    @Override
    protected String extractErrorData(KafkaResponseDTOWrapper<DuplicateResponseDTO> response) {
        return response.body().data().isEmpty() ? "" : response.body().data().toString();
    }

    @Override
    protected AiStep getStep() {
        return AiStep.DUPLICATE;
    }
}
