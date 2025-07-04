package ongi.ongibe.domain.ai.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.dto.CategoryResponseDTO;
import ongi.ongibe.domain.ai.dto.KafkaResponseDTOWrapper;
import ongi.ongibe.domain.ai.kafka.AiStepTransitionService;
import ongi.ongibe.domain.ai.producer.AiEmbeddingProducer;
import ongi.ongibe.domain.ai.repository.AiTaskStatusRepository;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.album.service.AlbumProcessService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiCategoryConsumer extends AbstractAiConsumer<KafkaResponseDTOWrapper<CategoryResponseDTO>>{

    private final PictureRepository pictureRepository;

    public AiCategoryConsumer(AiTaskStatusRepository aiTaskStatusRepository, AiStepTransitionService transitionService,
            AlbumProcessService albumProcessService, ObjectMapper objectMapper, AiEmbeddingProducer embeddingProducer, PictureRepository pictureRepository) {
        super(aiTaskStatusRepository, transitionService, albumProcessService, objectMapper, embeddingProducer);
        this.pictureRepository = pictureRepository;
    }

    @KafkaListener(
            topics = "${kafka.topic.response.category}",
            containerFactory = "genericKafkaListenerContainerFactory"
    )
    public void consume(List<KafkaResponseDTOWrapper<CategoryResponseDTO>> responses) {
        for(KafkaResponseDTOWrapper<CategoryResponseDTO> response : responses) {
            this.consume(response);
            if (response.statusCode() == 201) {
                Long albumId = response.albumId();
                CategoryResponseDTO dto = objectMapper.convertValue(response.body(), CategoryResponseDTO.class);
                List<CategoryResponseDTO.CategoryResult> categories = dto.data();
                for (var category : categories) {
                    pictureRepository.updateTag(albumId, category.images(), category.category());
                }
                log.info("[Category] album {} category {}개 분리 완료", albumId, categories.size());
            }
        }
    }

    @Override
    protected Long extractAlbumId(KafkaResponseDTOWrapper<CategoryResponseDTO> response) {
        return response.albumId();
    }

    @Override
    protected int extractStatusCode(KafkaResponseDTOWrapper<CategoryResponseDTO> response) {
        return response.statusCode();
    }

    @Override
    protected String extractTaskId(KafkaResponseDTOWrapper<CategoryResponseDTO> response) {
        return response.taskId();
    }

    @Override
    protected String extractMessage(KafkaResponseDTOWrapper<CategoryResponseDTO> response) {
        return response.body().message();
    }

    @Override
    protected String extractErrorData(KafkaResponseDTOWrapper<CategoryResponseDTO> response) {
        return response.body().data().isEmpty() ? "" : response.body().data().toString();
    }

    @Override
    protected AiStep getStep() {
        return AiStep.CATEGORY;
    }
}
