package ongi.ongibe.domain.ai.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.dto.AiEmbeddingResponseDTO;
import ongi.ongibe.domain.ai.dto.KafkaResponseDTOWrapper;
import ongi.ongibe.domain.ai.kafka.AiStepTransitionService;
import ongi.ongibe.domain.ai.producer.AiEmbeddingProducer;
import ongi.ongibe.domain.ai.repository.AiTaskStatusRepository;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.service.AlbumProcessService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiEmbeddingConsumer extends AbstractAiConsumer<KafkaResponseDTOWrapper<AiEmbeddingResponseDTO>> {

    public AiEmbeddingConsumer(AiTaskStatusRepository aiTaskStatusRepository, AiStepTransitionService transitionService, ObjectMapper objectMapper, AlbumProcessService albumProcessService,
            AiEmbeddingProducer embeddingProducer) {
        super(aiTaskStatusRepository, transitionService, albumProcessService, objectMapper, embeddingProducer);
    }

    @KafkaListener(
            topics = "${kafka.topic.response.embedding}",
            groupId = "be-group",
            containerFactory = "genericKafkaListenerContainerFactory"
    )
    public void consume(List<KafkaResponseDTOWrapper<AiEmbeddingResponseDTO>> responses) {
        for (KafkaResponseDTOWrapper<AiEmbeddingResponseDTO> res : responses) {
            this.consume(res);
        }
    }

    @Override
    protected Long extractAlbumId(KafkaResponseDTOWrapper<AiEmbeddingResponseDTO> response) {
        return response.albumId();
    }

    @Override
    protected int extractStatusCode(KafkaResponseDTOWrapper<AiEmbeddingResponseDTO> response) {
        return response.statusCode();
    }

    @Override protected String extractTaskId(KafkaResponseDTOWrapper<AiEmbeddingResponseDTO> r) { return r.taskId(); }
    @Override protected String extractMessage(KafkaResponseDTOWrapper<AiEmbeddingResponseDTO> r) { return r.body().message(); }

    @Override
    protected String extractErrorData(KafkaResponseDTOWrapper<AiEmbeddingResponseDTO> response) {
        return response.body().data().isEmpty() ? "" : String.join(", ", response.body().data());
    }

    @Override protected AiStep getStep() { return AiStep.EMBEDDING; }
}
