package ongi.ongibe.domain.ai.consumer;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.dto.AiEmbeddingResponseDTO;
import ongi.ongibe.domain.ai.dto.KafkaResponseDTOWrapper;
import ongi.ongibe.domain.ai.repository.AiTaskStatusRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiEmbeddingConsumer extends AbstractAiConsumer<KafkaResponseDTOWrapper<AiEmbeddingResponseDTO>> {

    public AiEmbeddingConsumer(AiTaskStatusRepository aiTaskStatusRepository){
        super(aiTaskStatusRepository);
    }

    @KafkaListener(
            topics = "#{'${kafka.topic.response.embedding}'}",
            containerFactory = "batchKafkaListenerContainerFactory"
    )
    public void consume(List<KafkaResponseDTOWrapper<AiEmbeddingResponseDTO>> responses) {
        for (KafkaResponseDTOWrapper<AiEmbeddingResponseDTO> res : responses) {
            this.consume(res);
        }
    }

    @Override protected String extractTaskId(KafkaResponseDTOWrapper<AiEmbeddingResponseDTO> r) { return r.taskId(); }
    @Override protected String extractMessage(KafkaResponseDTOWrapper<AiEmbeddingResponseDTO> r) { return r.body().message(); }

    @Override
    protected String extractErrorData(KafkaResponseDTOWrapper<AiEmbeddingResponseDTO> response) {
        return response.body().data().isEmpty() ? "" : String.join(", ", response.body().data());
    }

    @Override protected AiStep getStep() { return AiStep.EMBEDDING; }
}
