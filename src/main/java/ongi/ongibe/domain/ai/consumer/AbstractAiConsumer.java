package ongi.ongibe.domain.ai.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.entity.AiTaskStatus;
import ongi.ongibe.domain.ai.repository.AiTaskStatusRepository;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractAiConsumer<T> implements AiConsumerInterface<T> {

    private final AiTaskStatusRepository taskStatusRepository;

    @Override
    public void consume(T response) {
        String taskId = extractTaskId(response);
        try {
            AiTaskStatus task = taskStatusRepository.findById(taskId)
                    .orElseThrow(() -> new IllegalArgumentException("task_id 없음: " + taskId));

            switch (extractMessage(response)) {
                case "success" -> task.markSuccess();
                case "internal_server_error" -> task.markRetryOrFail("서버 오류");
                case "invalid_request", "unauthorized_server" -> task.markFailed("요청/인증 오류");
                case "invalid_image_url" -> task.markFailed("잘못된 이미지: " + extractErrorData(response));
                default -> task.markFailed("알 수 없는 오류: " + extractMessage(response));
            }

            taskStatusRepository.save(task);
        } catch (Exception e) {
            log.error("[{}] 처리 실패: {}", getStep(), e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    protected abstract String extractTaskId(T response);
    protected abstract String extractMessage(T response);
    protected abstract AiStep getStep();
}
