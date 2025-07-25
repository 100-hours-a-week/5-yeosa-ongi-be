package ongi.ongibe.domain.ai.producer;

import com.github.f4b6a3.ulid.UlidCreator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStatus;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.aiInterface.AiClusterServiceInterface;
import ongi.ongibe.domain.ai.dto.AiImageRequestDTO;
import ongi.ongibe.domain.ai.dto.KafkaRequestDTOWrapper;
import ongi.ongibe.domain.ai.entity.AiTaskStatus;
import ongi.ongibe.domain.ai.repository.AiTaskStatusRepository;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.global.util.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiClusterProducer implements AiClusterServiceInterface {

    @Value("${kafka.topic.request.people}")
    private String requestTopic;
    private final AiKafkaProducer kafkaProducer;
    private final PictureRepository pictureRepository;
    private final AiTaskStatusRepository taskStatusRepository;

    @Override
    public void requestCluster(Long albumId, Long userId, List<String> s3keys) {
        String topic = AiStep.PEOPLE.toString();
        String taskId = topic + "-" + UlidCreator.getUlid().toString();
        AiTaskStatus taskStatus = AiTaskStatus.builder()
                .taskId(taskId)
                .step(AiStep.PEOPLE)
                .status(AiStatus.PENDING)
                .userId(userId)
                .albumId(albumId)
                .s3keysJson(JsonUtil.toJson(s3keys))
                .retryCount(0)
                .build();
        taskStatusRepository.save(taskStatus);

        List<String> filteredKeys = pictureRepository.findAllByAlbumId(albumId).stream()
                .map(Picture::getS3Key)
                .toList();
        KafkaRequestDTOWrapper<AiImageRequestDTO> dto = new KafkaRequestDTOWrapper<>(taskId, albumId, new AiImageRequestDTO(filteredKeys));
        kafkaProducer.send(requestTopic, topic + userId, dto);

        taskStatus.markInProgress();
        taskStatusRepository.save(taskStatus);
    }
}
