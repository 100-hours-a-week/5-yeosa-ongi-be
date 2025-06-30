package ongi.ongibe.domain.ai.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.ulid.UlidCreator;
import io.swagger.v3.core.util.Json;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStatus;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.aiInterface.AiEmbeddingServiceInterface;
import ongi.ongibe.domain.ai.dto.AiImageRequestDTO;
import ongi.ongibe.domain.ai.dto.KafkaRequestDTOWrapper;
import ongi.ongibe.domain.ai.entity.AiTaskStatus;
import ongi.ongibe.domain.ai.repository.AiTaskStatusRepository;
import ongi.ongibe.domain.album.AlbumProcessState;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.exception.AlbumException;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.global.util.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiEmbeddingProducer implements AiEmbeddingServiceInterface {

    @Value("${kafka.topic.request.embedding}")
    private String requestTopic;
    private final AiKafkaProducer aiKafkaProducer;
    private final AiTaskStatusRepository taskStatusRepository;
    private final AlbumRepository albumRepository;

    @Override
    @Transactional
    public void requestEmbeddings(Long albumId, Long userId, List<String> s3keys) {
        String topic = AiStep.EMBEDDING.toString();
        String taskId = topic + "-" + UlidCreator.getUlid().toString();
        String s3keyjson = JsonUtil.toJson(s3keys);
        log.info("Sending embedding request to topic : {}", requestTopic);
        log.info("albumId {}, userId {}, s3keys {}", albumId,  userId, s3keyjson);
        AiTaskStatus taskStatus = AiTaskStatus.builder()
                .taskId(taskId)
                .step(AiStep.EMBEDDING)
                .status(AiStatus.PENDING)
                .userId(userId)
                .albumId(albumId)
                .s3keysJson(s3keyjson)
                .retryCount(0)
                .build();
        taskStatusRepository.save(taskStatus);

        KafkaRequestDTOWrapper<AiImageRequestDTO> dto = new KafkaRequestDTOWrapper<>(taskId, albumId, new AiImageRequestDTO(s3keys));
        aiKafkaProducer.send(requestTopic, topic + userId, dto);
        taskStatus.markInProgress();
        taskStatusRepository.save(taskStatus);
    }

    public void reRequestEmbeddings(String taskId) {
        String topic = AiStep.EMBEDDING.toString();
        AiTaskStatus taskStatus = taskStatusRepository.findById(taskId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found")
        );

        KafkaRequestDTOWrapper<AiImageRequestDTO> dto = new KafkaRequestDTOWrapper<>(
                taskId, taskStatus.getAlbumId(),
                new AiImageRequestDTO(JsonUtil.fromJson(taskStatus.getS3keysJson()))
        );
        aiKafkaProducer.send(requestTopic, topic + taskStatus.getUserId(), dto);
    }
}
