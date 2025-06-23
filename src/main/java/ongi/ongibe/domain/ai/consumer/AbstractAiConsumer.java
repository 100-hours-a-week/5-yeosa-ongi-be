package ongi.ongibe.domain.ai.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.entity.AiTaskStatus;
import ongi.ongibe.domain.ai.kafka.AiStepTransitionService;
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

            int statusCode = extractStatusCode(response);
            switch (statusCode) {
                case 201 -> {
                    task.markSuccess();
                    taskStatusRepository.save(task);
                }
                case 428 -> {
                    task.markRetryOrFail("임베딩 필요 : " + extractErrorData(response));
                    task.markStepEmbedding();
                }
                case 500 -> task.markRetryOrFail("서버 오류");
                case 400, 403 -> task.markFailed("요청/인증 오류");
                case 422 -> task.markRetryOrFail("잘못된 이미지: " + extractErrorData(response));
                default -> task.markRetryOrFail("알 수 없는 오류: " + extractMessage(response));
            }

            taskStatusRepository.save(task);
        } catch (Exception e) {
            log.error("[{}] 처리 실패: {}", getStep(), e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    protected abstract int extractStatusCode(T response);
    protected abstract String extractTaskId(T response);
    protected abstract String extractMessage(T response);
    protected abstract String extractErrorData(T response);
    protected abstract AiStep getStep();
}
