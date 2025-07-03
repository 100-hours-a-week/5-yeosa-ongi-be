package ongi.ongibe.domain.ai.kafka;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.aiInterface.AiAlbumServiceInterface;
import ongi.ongibe.domain.ai.producer.AiEmbeddingProducer;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Primary
//@Qualifier("kafka")
@Service
@RequiredArgsConstructor
@Slf4j
public class AiKafkaAlbumService implements AiAlbumServiceInterface {

    private final AiEmbeddingProducer embeddingProducer;
    private final AlbumRepository albumRepository;

    @Override
    @Async
    public void process(Album album, Long userId, List<String> s3keys,  List<String> concepts) {
        Long albumId = album.getId();
        log.info("🔥 트랜잭션 커밋 이후 Kafka 전송 시작 - albumId: {}", albumId);
        Album validAlbum = albumRepository.findById(albumId).orElse(null);
        log.info("앨범 정체 : {}",  validAlbum);
        embeddingProducer.requestEmbeddings(albumId, userId, s3keys);
    }
}
