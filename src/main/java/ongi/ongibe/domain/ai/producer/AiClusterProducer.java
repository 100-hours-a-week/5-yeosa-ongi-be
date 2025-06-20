package ongi.ongibe.domain.ai.producer;

import com.github.f4b6a3.ulid.UlidCreator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.aiInterface.AiClusterServiceInterface;
import ongi.ongibe.domain.ai.dto.AiImageRequestDTO;
import ongi.ongibe.domain.ai.dto.KafkaDTOWrapper;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiClusterProducer implements AiClusterServiceInterface {

    @Value("${kafka.topic.request.category}")
    private String requestTopic;
    private final AiKafkaProducer kafkaProducer;
    private final PictureRepository pictureRepository;

    @Override
    public void requestCluster(Long albumId, Long userId, List<String> s3keys) {
        String taskId = "people-" + UlidCreator.getUlid().toString();

        List<String> filteredKeys = pictureRepository.findAllByAlbumId(albumId).stream()
                .map(Picture::getS3Key)
                .toList();
        KafkaDTOWrapper<AiImageRequestDTO> dto = new KafkaDTOWrapper<>(taskId, albumId, new AiImageRequestDTO(filteredKeys));
        kafkaProducer.send(requestTopic, userId.toString(), dto);
    }
}
