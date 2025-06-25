package ongi.ongibe.domain.ai.kafka;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.aiInterface.AiAlbumServiceInterface;
import ongi.ongibe.domain.ai.entity.AiTaskStatus;
import ongi.ongibe.domain.ai.producer.AiEmbeddingProducer;
import ongi.ongibe.domain.ai.repository.AiTaskStatusRepository;
import ongi.ongibe.domain.album.entity.Album;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

//@Qualifier("kafka")
@Primary
@Service
@RequiredArgsConstructor
@Slf4j
public class AiKafkaAlbumService implements AiAlbumServiceInterface {

    private final AiEmbeddingProducer embeddingProducer;

    @Override
    public void process(Album album, Long userId, List<String> s3keys) {
        log.info("Processing album for kafka : {}", album);
        Long albumId = album.getId();
        embeddingProducer.requestEmbeddings(albumId, userId, s3keys);
    }
}
