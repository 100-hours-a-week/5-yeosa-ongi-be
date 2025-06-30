package ongi.ongibe.domain.ai.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.entity.AiTaskStatus;
import ongi.ongibe.domain.ai.kafka.AiStepTransitionService;
import ongi.ongibe.domain.ai.repository.AiTaskStatusRepository;
import ongi.ongibe.domain.album.AlbumProcessState;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.exception.AlbumException;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.global.util.JsonUtil;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractAiConsumer<T> implements AiConsumerInterface<T> {

    private final AiTaskStatusRepository taskStatusRepository;
    private final AiStepTransitionService stepTransitionService;
    private final AlbumRepository albumRepository;

    @Override
    public void consume(T response) {
        String taskId = extractTaskId(response);
        log.info("[DEBUG] 컨슘 시작, 아이디 : {} ",  taskId);
        Long albumId = extractAlbumId(response);
        Album album = albumRepository.findById(albumId).orElseThrow(
                () -> new AlbumException(HttpStatus.NOT_FOUND, "Album Not Found when kafka consume")
        );
        try {
            AiTaskStatus task = taskStatusRepository.findById(taskId)
                    .orElseThrow(() -> new IllegalArgumentException("task_id 없음: " + taskId));

            int statusCode = extractStatusCode(response);
            switch (statusCode) {
                case 201 -> {
                    task.markSuccess();
                    taskStatusRepository.save(task);
                    List<String> s3keys = JsonUtil.fromJson(task.getS3keysJson());
                    stepTransitionService.handleStepCondition(task, s3keys);
                }
                case 428 -> {
                    task.markRetryOrFail("임베딩 필요 : " + extractErrorData(response));
                    task.markStepEmbedding();
                    album.setProcessState(AlbumProcessState.FAILED);
                }
                case 500 -> {
                    task.markRetryOrFail("서버 오류");
                    album.setProcessState(AlbumProcessState.FAILED);
                }
                case 400, 403 -> {
                    task.markFailed("요청/인증 오류");
                    album.setProcessState(AlbumProcessState.FAILED);
                }
                case 422 -> {
                    task.markRetryOrFail("잘못된 이미지: " + extractErrorData(response));
                    album.setProcessState(AlbumProcessState.FAILED);
                }
                default -> {
                    task.markRetryOrFail("알 수 없는 오류: " + extractMessage(response));
                    album.setProcessState(AlbumProcessState.FAILED);
                }
            }

            taskStatusRepository.save(task);
            albumRepository.save(album);
        } catch (Exception e) {
            log.error("[{}] 처리 실패: {}", getStep(), e.getMessage(), e);
            album.setProcessState(AlbumProcessState.FAILED);
            albumRepository.save(album);
            throw new RuntimeException(e);
        }
    }

    protected abstract Long extractAlbumId(T response);
    protected abstract int extractStatusCode(T response);
    protected abstract String extractTaskId(T response);
    protected abstract String extractMessage(T response);
    protected abstract String extractErrorData(T response);
    protected abstract AiStep getStep();
}
