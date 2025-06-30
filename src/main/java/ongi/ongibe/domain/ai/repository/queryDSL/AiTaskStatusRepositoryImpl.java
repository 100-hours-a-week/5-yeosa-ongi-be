package ongi.ongibe.domain.ai.repository.queryDSL;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.domain.ai.AiStatus;
import ongi.ongibe.domain.ai.entity.AiTaskStatus;
import ongi.ongibe.domain.ai.entity.QAiTaskStatus;

@RequiredArgsConstructor
public class AiTaskStatusRepositoryImpl implements AiTaskStatusRepositoryQueryDSL {

    private final JPAQueryFactory queryFactory;

    @Override
    public int countSuccessStepsByAlbumId(Long albumId) {
        QAiTaskStatus ats = QAiTaskStatus.aiTaskStatus;

        List<AiTaskStatus> latestSuccessTasks = queryFactory
                .selectFrom(ats)
                .where(
                        ats.albumId.eq(albumId),
                        ats.status.eq(AiStatus.SUCCESS),
                        ats.createdAt.in(
                                queryFactory
                                        .select(ats.createdAt.max())
                                        .from(ats)
                                        .where(ats.albumId.eq(albumId))
                                        .groupBy(ats.step)
                        )
                )
                .fetch();

        return (int) latestSuccessTasks.stream()
                .map(AiTaskStatus::getStep)
                .distinct()
                .count();
    }
}
