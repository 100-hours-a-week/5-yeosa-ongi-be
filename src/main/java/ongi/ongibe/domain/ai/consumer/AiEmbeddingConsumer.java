package ongi.ongibe.domain.ai.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiEmbeddingConsumer {

    @Value("${kafka.topic.response.embedding}")
    private String responseTopic;

    @KafkaListener()
    public void handleEmbeddingResponse(){

    }
}
