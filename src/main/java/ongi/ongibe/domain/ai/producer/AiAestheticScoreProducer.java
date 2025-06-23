package ongi.ongibe.domain.ai.producer;

import com.github.f4b6a3.ulid.UlidCreator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.aiInterface.AiAestheticServiceInterface;
import ongi.ongibe.domain.ai.dto.AiAestheticScoreRequestDTO;
import ongi.ongibe.domain.ai.dto.KafkaRequestDTOWrapper;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAestheticScoreProducer implements AiAestheticServiceInterface {

    @Value("${kafka.topic.request.aesthetic}")
    private String requestTopic;
    private final AiKafkaProducer aiKafkaProducer;
    private final PictureRepository pictureRepository;

    @Override
    public void requestAestheticScores(Long albumId, Long userId, List<String> s3keys) {
        String topic = AiStep.AESTHETIC.toString();
        String taskId = topic + "-" + UlidCreator.getUlid().toString();

        List<Picture> pictures = pictureRepository.findAllByS3KeyIn(s3keys);
        AiAestheticScoreRequestDTO request = AiAestheticScoreRequestDTO.from(pictures);
        KafkaRequestDTOWrapper<AiAestheticScoreRequestDTO> dto = new KafkaRequestDTOWrapper<>(taskId, albumId, request);

        aiKafkaProducer.send(requestTopic, topic + userId, dto);
    }
}
