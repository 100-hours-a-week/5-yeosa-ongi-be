package ongi.ongibe.domain.ai.consumer;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.dto.DuplicateResponseDTO;
import ongi.ongibe.domain.ai.dto.KafkaResponseDTOWrapper;
import ongi.ongibe.domain.ai.repository.AiTaskStatusRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiDuplicateConsumer extends AbstractAiConsumer<KafkaResponseDTOWrapper<DuplicateResponseDTO>> {

    private final PictureRepository pictureRepository;

    public AiDuplicateConsumer(AiTaskStatusRepository aiTaskStatusRepository,
            PictureRepository pictureRepository) {
        super(aiTaskStatusRepository);
        this.pictureRepository = pictureRepository;
    }

    @KafkaListener(
            topics = "#{'${kafka.topic.response.duplicate}'}",
            containerFactory = "batchKafkaListenerContainerFactory"
    )
    public void consume(List<KafkaResponseDTOWrapper<DuplicateResponseDTO>> responses) {
        for(KafkaResponseDTOWrapper<DuplicateResponseDTO> response : responses) {
            this.consume(response);
            if (response.body().message().equals("success")) {
                Long albumId = response.albumId();
                List<String> duplicateKeys = response.body().data().stream()
                        .flatMap(List::stream)
                        .toList();
                pictureRepository.markPicturesAsDuplicated(albumId, duplicateKeys);
                log.info("[DUPLICATE] album {} 중복 {}장 처리 완료", albumId, duplicateKeys.size());
            }
        }
    }

    @Override
    protected String extractTaskId(KafkaResponseDTOWrapper<DuplicateResponseDTO> response) {
        return response.taskId();
    }

    @Override
    protected String extractMessage(KafkaResponseDTOWrapper<DuplicateResponseDTO> response) {
        return response.body().message();
    }

    @Override
    protected String extractErrorData(KafkaResponseDTOWrapper<DuplicateResponseDTO> response) {
        return "";
    }

    @Override
    protected AiStep getStep() {
        return AiStep.DUPLICATE;
    }
}
