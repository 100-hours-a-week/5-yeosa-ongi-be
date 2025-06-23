package ongi.ongibe.domain.ai.consumer;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.dto.AiAestheticScoreResponseDTO;
import ongi.ongibe.domain.ai.dto.KafkaResponseDTOWrapper;
import ongi.ongibe.domain.ai.repository.AiTaskStatusRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiAestheticComsumer extends AbstractAiConsumer<KafkaResponseDTOWrapper<AiAestheticScoreResponseDTO>>{

    private final PictureRepository pictureRepository;

    public AiAestheticComsumer(AiTaskStatusRepository aiTaskStatusRepository, PictureRepository pictureRepository) {
        super(aiTaskStatusRepository);
        this.pictureRepository = pictureRepository;
    }

    @KafkaListener(
            topics = "#{'${kafka.topic.response.aesthetic'}",
            containerFactory = "batchKafkaListenerContainerFactory"
    )
    public void consume(List<KafkaResponseDTOWrapper<AiAestheticScoreResponseDTO>> responses) {
        for (KafkaResponseDTOWrapper<AiAestheticScoreResponseDTO> response : responses) {
            this.consume(response);
            if (response.body().message().equals("success")) {
                Long albumId = response.albumId();
                List<AiAestheticScoreResponseDTO.ScoreCategory> scores = response.body().data();
                for (var category : scores) {
                    for (var entry : category.images()) {
                        pictureRepository.updateScore(albumId, entry.image(), entry.score());
                    }
                }
                log.info("[Aesthetic] album {}, {}개 카테고리 처리 완료", albumId, scores.size());
            }
        }
    }

    @Override
    protected String extractTaskId(KafkaResponseDTOWrapper<AiAestheticScoreResponseDTO> response) {
        return response.taskId();
    }

    @Override
    protected String extractMessage(KafkaResponseDTOWrapper<AiAestheticScoreResponseDTO> response) {
        return response.body().data().isEmpty() ? "" : response.body().data().toString();
    }

    @Override
    protected String extractErrorData(
            KafkaResponseDTOWrapper<AiAestheticScoreResponseDTO> response) {
        return "";
    }

    @Override
    protected AiStep getStep() {
        return AiStep.AESTHETIC;
    }
}
