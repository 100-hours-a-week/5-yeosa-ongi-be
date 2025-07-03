package ongi.ongibe.domain.ai.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class dltListener {

    @KafkaListener(topics = "album.ai.embedding.response.DLT", groupId = "debug-group")
    public void listenDLTembedding(String message, @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String errorMsg) {
        log.error("💥 DLT Message: {}, Reason: {}", message, errorMsg);
    }

    @KafkaListener(topics = "album.ai.category.response.DLT", groupId = "debug-group")
    public void listenDLT(String message, @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String errorMsg) {
        log.error("💥 DLT Message: {}, Reason: {}", message, errorMsg);
    }

    @KafkaListener(topics = "album.ai.people.response.DLT", groupId = "debug-group")
    public void listenDLTpeople(String message, @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String errorMsg) {
        log.error("💥 DLT Message: {}, Reason: {}", message, errorMsg);
    }


}
