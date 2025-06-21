package ongi.ongibe.domain.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Getter;
import ongi.ongibe.domain.ai.AiStatus;
import ongi.ongibe.domain.ai.AiStep;

@Entity
@Getter
public class AiTaskStatus {

    @Id
    private String taskId;

    @Enumerated(EnumType.STRING)
    private AiStep step;

    @Enumerated(EnumType.STRING)
    private AiStatus status;

    private Long userId;
    private Long albumId;
    private int retryCount;

    @Column(columnDefinition = "TEXT")
    private String errorMsg;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public void markPending() {
        this.status = AiStatus.PENDING;
    }

    public void markInProgress() {
        this.status = AiStatus.IN_PROGRESS;
    }

    public void markSuccess() {
        this.status = AiStatus.SUCCESS;
        this.errorMsg = null;
    }

    public void markFailed(String reason) {
        this.status = AiStatus.FAILED;
        this.errorMsg = reason;
    }

    public void markRetryOrFail(String reason) {
        this.retryCount++;
        if (retryCount >= 3) {
            this.status = AiStatus.FAILED;
        } else {
            this.status = AiStatus.RETRY;
        }
        this.errorMsg = reason;
    }
}

