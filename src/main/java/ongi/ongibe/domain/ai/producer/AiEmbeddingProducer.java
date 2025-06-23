package ongi.ongibe.domain.ai.producer;

import com.github.f4b6a3.ulid.UlidCreator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.aiInterface.AiEmbeddingServiceInterface;
import ongi.ongibe.domain.ai.dto.AiImageRequestDTO;
import ongi.ongibe.domain.ai.dto.KafkaRequestDTOWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiEmbeddingProducer implements AiEmbeddingServiceInterface {

    @Value("${kafka.topic.request.embedding}")
    private String requestTopic;
    private final AiKafkaProducer aiKafkaProducer;

    @Override
    public void requestEmbeddings(Long albumId, Long userId, List<String> s3keys) {
        String topic = AiStep.EMBEDDING.toString();
        String taskId = topic + "-" + UlidCreator.getUlid().toString();
        KafkaRequestDTOWrapper<AiImageRequestDTO> dto = new KafkaRequestDTOWrapper<>(taskId, albumId, new AiImageRequestDTO(s3keys));
        aiKafkaProducer.send(requestTopic, topic + userId, dto);
    }
}
