package ongi.ongibe.domain.ai.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAiTaskStatus is a Querydsl query type for AiTaskStatus
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAiTaskStatus extends EntityPathBase<AiTaskStatus> {

    private static final long serialVersionUID = 41787249L;

    public static final QAiTaskStatus aiTaskStatus = new QAiTaskStatus("aiTaskStatus");

    public final NumberPath<Long> albumId = createNumber("albumId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath errorMsg = createString("errorMsg");

    public final NumberPath<Integer> retryCount = createNumber("retryCount", Integer.class);

    public final StringPath s3keysJson = createString("s3keysJson");

    public final EnumPath<ongi.ongibe.domain.ai.AiStatus> status = createEnum("status", ongi.ongibe.domain.ai.AiStatus.class);

    public final EnumPath<ongi.ongibe.domain.ai.AiStep> step = createEnum("step", ongi.ongibe.domain.ai.AiStep.class);

    public final StringPath taskId = createString("taskId");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QAiTaskStatus(String variable) {
        super(AiTaskStatus.class, forVariable(variable));
    }

    public QAiTaskStatus(Path<? extends AiTaskStatus> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAiTaskStatus(PathMetadata metadata) {
        super(AiTaskStatus.class, metadata);
    }

}

