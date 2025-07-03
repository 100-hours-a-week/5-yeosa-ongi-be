package ongi.ongibe.domain.ai.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ongi.ongibe.domain.ai.AiStatus;
import ongi.ongibe.domain.ai.AiStep;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(columnDefinition = "TEXT")
    private String s3keysJson;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public void markStepEmbedding(){
        this.step = AiStep.EMBEDDING;
        this.errorMsg = null;
    }

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

