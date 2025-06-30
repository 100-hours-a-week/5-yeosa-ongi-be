package ongi.ongibe.domain.ai.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStatus;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.dto.AiErrorResponseDTO;
import ongi.ongibe.domain.ai.dto.KafkaResponseDTOWrapper;
import ongi.ongibe.domain.ai.entity.AiTaskStatus;
import ongi.ongibe.domain.ai.kafka.AiStepTransitionService;
import ongi.ongibe.domain.ai.producer.AiEmbeddingProducer;
import ongi.ongibe.domain.ai.repository.AiTaskStatusRepository;
import ongi.ongibe.domain.album.AlbumProcessState;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.exception.AlbumException;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.service.AlbumProcessService;
import ongi.ongibe.global.util.JsonUtil;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractAiConsumer<T extends KafkaResponseDTOWrapper<?>> implements AiConsumerInterface<T> {

    private final AiTaskStatusRepository taskStatusRepository;
    private final AiStepTransitionService stepTransitionService;
    private final AlbumProcessService albumProcessService;
    protected final ObjectMapper objectMapper;
    private final AiEmbeddingProducer embeddingProducer;

    @Override
    public void consume(T response) {
        String taskId = extractTaskId(response);
        int statusCode = extractStatusCode(response);
        Long albumId = extractAlbumId(response);
        log.info("[DEBUG] 컨슘 시작, 아이디 : {}, status code : {}, albumId : {}",  taskId, statusCode, albumId);
        try {
            AiTaskStatus task = taskStatusRepository.findById(taskId)
                    .orElseThrow(() -> new IllegalArgumentException("task_id 없음: " + taskId));

            if (statusCode == 201) {
                handleSuccess(task, albumId, response.body());
            } else {
                handleError(task, albumId, statusCode, response.body());
            }

            taskStatusRepository.save(task);
        } catch (Exception e) {
            log.error("[{}] 처리 실패: {}", getStep(), e.getMessage(), e);
            albumProcessService.markProcess(albumId, AlbumProcessState.FAILED);
            throw new RuntimeException(e);
        }
    }

    private void handleSuccess(AiTaskStatus task, Long albumId, Object body) {
        task.markSuccess();
        List<String> s3keys = JsonUtil.fromJson(task.getS3keysJson());
        stepTransitionService.handleStepCondition(task, s3keys);
        checkAndMarkAlbumDoneIfAllStepsSucceeded(albumId);
    }

    private void handleError(AiTaskStatus task, Long albumId, int statusCode, Object body) {
        AiErrorResponseDTO error = objectMapper.convertValue(body, AiErrorResponseDTO.class);
        String message = error.message();
        List<String> data = error.data();

        switch (statusCode) {
            case 428 -> {
                task.markRetryOrFail("임베딩 필요 : " + data);
                task.markStepEmbedding();
                embeddingProducer.reRequestEmbeddings(task.getTaskId());
            }
            case 500 -> task.markRetryOrFail("서버 오류: " + message);
            case 400, 403 -> task.markFailed("요청/인증 오류: " + message);
            case 422 -> task.markRetryOrFail("잘못된 이미지: " + data);
            default -> task.markRetryOrFail("알 수 없는 오류: " + message);
        }

        albumProcessService.markProcess(albumId, AlbumProcessState.FAILED);
    }

    protected abstract Long extractAlbumId(T response);
    protected abstract int extractStatusCode(T response);
    protected abstract String extractTaskId(T response);
    protected abstract String extractMessage(T response);
    protected abstract String extractErrorData(T response);
    protected abstract AiStep getStep();

    private void checkAndMarkAlbumDoneIfAllStepsSucceeded(Long albumId) {
        int successCount = taskStatusRepository.countSuccessStepsByAlbumId(albumId);
        if (successCount == AiStep.values().length) {
            albumProcessService.markProcess(albumId, AlbumProcessState.DONE);
            log.info("[AI] Album {} 모든 단계 성공 → DONE 처리 완료", albumId);
        } else {
            log.info("[AI] Album {} 아직 완료되지 않은 단계 존재 ({} / {})", albumId, successCount, AiStep.values().length);
        }
    }
}
