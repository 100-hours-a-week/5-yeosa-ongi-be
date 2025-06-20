package ongi.ongibe.domain.ai.producer;

import com.github.f4b6a3.ulid.UlidCreator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.dto.AiImageRequestDTO;
import ongi.ongibe.domain.ai.dto.KafkaDTOWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiDuplicateProducer {

    @Value("${kafka.topic.request.duplicate}")
    private String requestTopic;
    private final AiKafkaProducer aiKafkaProducer;

    public void requestDuplicate(Long albumId, Long userId, List<String> s3keys) {
        String taskId = "duplicate-" + UlidCreator.getUlid().toString();
        KafkaDTOWrapper<AiImageRequestDTO> dto = new KafkaDTOWrapper<>(taskId, albumId, new AiImageRequestDTO(s3keys));
        aiKafkaProducer.send(requestTopic, userId.toString(), dto);
    }

}
