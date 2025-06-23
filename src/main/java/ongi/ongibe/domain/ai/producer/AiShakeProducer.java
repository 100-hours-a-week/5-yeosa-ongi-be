package ongi.ongibe.domain.ai.producer;

import com.github.f4b6a3.ulid.UlidCreator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.dto.AiImageRequestDTO;
import ongi.ongibe.domain.ai.dto.KafkaRequestDTOWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiShakeProducer {

    @Value("${kafka.topic.request.quality}")
    private String requestTopic;
    private final AiKafkaProducer aiKafkaProducer;

    public void requestShaky(Long albumId, Long userId, List<String> s3keys) {
        String topic = AiStep.QUALITY.toString();
        String taskId = topic + "-" + UlidCreator.getUlid().toString();
        KafkaRequestDTOWrapper<AiImageRequestDTO> dto = new KafkaRequestDTOWrapper<>(taskId, albumId, new AiImageRequestDTO(s3keys));
        aiKafkaProducer.send(requestTopic, topic + userId, dto);
    }
}
