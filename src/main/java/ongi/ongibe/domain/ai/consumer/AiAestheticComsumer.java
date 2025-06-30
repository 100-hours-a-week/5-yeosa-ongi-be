package ongi.ongibe.domain.ai.consumer;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.dto.AiAestheticScoreResponseDTO;
import ongi.ongibe.domain.ai.dto.KafkaResponseDTOWrapper;
import ongi.ongibe.domain.ai.kafka.AiStepTransitionService;
import ongi.ongibe.domain.ai.repository.AiTaskStatusRepository;
import ongi.ongibe.domain.album.AlbumProcessState;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.album.service.AlbumProcessService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiAestheticComsumer extends AbstractAiConsumer<KafkaResponseDTOWrapper<AiAestheticScoreResponseDTO>>{

    private final PictureRepository pictureRepository;

    public AiAestheticComsumer(AiTaskStatusRepository aiTaskStatusRepository, AiStepTransitionService transitionService, PictureRepository pictureRepository, AlbumProcessService albumProcessService) {
        super(aiTaskStatusRepository, transitionService, albumProcessService);
        this.pictureRepository = pictureRepository;
    }

    @KafkaListener(
            topics = "${kafka.topic.response.score}",
            containerFactory = "aestheticKafkaListenerContainerFactory"
    )
    public void consume(List<KafkaResponseDTOWrapper<AiAestheticScoreResponseDTO>> responses) {
        for (KafkaResponseDTOWrapper<AiAestheticScoreResponseDTO> response : responses) {
            this.consume(response);
            if (response.statusCode() == 201) {
                Long albumId = response.albumId();
                List<AiAestheticScoreResponseDTO.ScoreCategory> scores = response.body().data();
                for (var category : scores) {
                    for (var entry : category.images()) {
                        pictureRepository.updateScore(albumId, entry.image(), entry.score());
                    }
                }
                log.info("[Aesthetic] album {}, {}개 카테고리 처리 완료", albumId, scores.size());
            }
            albumProcessService.markProcess(response.albumId(), AlbumProcessState.DONE);
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
        return response.body().message();
    }

    @Override
    protected String extractErrorData(
            KafkaResponseDTOWrapper<AiAestheticScoreResponseDTO> response) {
        return response.body().data().isEmpty() ? "" : response.body().data().toString();
    }

    @Override
    protected AiStep getStep() {
        return AiStep.SCORE;
    }
}
