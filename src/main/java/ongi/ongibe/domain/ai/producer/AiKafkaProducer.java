package ongi.ongibe.domain.ai.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class AiKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void send(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload);
        log.info("Sent message to kafka topic : {}", topic);
    }
}
