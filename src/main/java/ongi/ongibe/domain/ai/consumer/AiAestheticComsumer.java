package ongi.ongibe.domain.ai.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.dto.AiAestheticScoreResponseDTO;
import ongi.ongibe.domain.ai.dto.AiErrorResponseDTO;
import ongi.ongibe.domain.ai.dto.KafkaResponseDTOWrapper;
import ongi.ongibe.domain.ai.kafka.AiStepTransitionService;
import ongi.ongibe.domain.ai.producer.AiEmbeddingProducer;
import ongi.ongibe.domain.ai.repository.AiTaskStatusRepository;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.album.service.AlbumMarkService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiAestheticComsumer extends AbstractAiConsumer<KafkaResponseDTOWrapper<AiAestheticScoreResponseDTO>>{

    private final PictureRepository pictureRepository;

    public AiAestheticComsumer(
            AiTaskStatusRepository aiTaskStatusRepository, AiStepTransitionService transitionService, AiEmbeddingProducer embeddingProducer, ObjectMapper objectMapper,PictureRepository pictureRepository, AlbumMarkService albumMarkService) {
        super(aiTaskStatusRepository, transitionService, albumMarkService, objectMapper, embeddingProducer);
        this.pictureRepository = pictureRepository;
    }

    @KafkaListener(
            topics = "${kafka.topic.response.score}",
            containerFactory = "genericKafkaListenerContainerFactory"
    )
    public void consume(List<KafkaResponseDTOWrapper<AiAestheticScoreResponseDTO>> responses) {
        for (KafkaResponseDTOWrapper<AiAestheticScoreResponseDTO> response : responses) {
            if (response.statusCode() == 201) {
                Long albumId = response.albumId();

                AiAestheticScoreResponseDTO body = objectMapper.convertValue(
                        response.body(), AiAestheticScoreResponseDTO.class);

                List<AiAestheticScoreResponseDTO.ScoreCategory> scores = body.data();
                List<String> s3keys = scores.stream()
                        .flatMap(category -> category.images().stream())
                        .map(AiAestheticScoreResponseDTO.ScoreCategory.ScoreEntry::image)
                        .toList();

                Map<String, Picture> pictureMap = pictureRepository.findAllByS3KeyIn(s3keys).stream()
                        .collect(Collectors.toMap(Picture::getS3Key, p -> p));

                for (var category : scores) {
                    for (var entry : category.images()) {
                        Picture picture = pictureMap.get(entry.image());
                        if (picture != null) {
                            picture.setQualityScore((float) entry.score());
                        }
                    }
                }
                pictureRepository.saveAll(pictureMap.values());
                log.info("[Aesthetic] album {}, {}개 카테고리 처리 완료", albumId, scores.size());
            }
            this.consume(response);
        }
    }

    @Override
    protected Long extractAlbumId(KafkaResponseDTOWrapper<AiAestheticScoreResponseDTO> response) {
        return response.albumId();
    }

    @Override
    protected int extractStatusCode(KafkaResponseDTOWrapper<AiAestheticScoreResponseDTO> response) {
        return response.statusCode();
    }

    @Override
    protected String extractTaskId(KafkaResponseDTOWrapper<AiAestheticScoreResponseDTO> response) {
        return response.taskId();
    }

    @Override
    protected String extractMessage(KafkaResponseDTOWrapper<AiAestheticScoreResponseDTO> response) {
        if (response.statusCode() == 201) {
            return response.body().message();
        }
        var error = objectMapper.convertValue(response.body(), AiErrorResponseDTO.class);
        return error.message();
    }


    @Override
    protected String extractErrorData(KafkaResponseDTOWrapper<AiAestheticScoreResponseDTO> response) {
        if (response.statusCode() == 201) {
            return response.body().data().toString();
        }
        var error = objectMapper.convertValue(response.body(), AiErrorResponseDTO.class);
        return error.data() == null ? "" : error.data().toString();
    }

    @Override
    protected AiStep getStep() {
        return AiStep.SCORE;
    }
}
