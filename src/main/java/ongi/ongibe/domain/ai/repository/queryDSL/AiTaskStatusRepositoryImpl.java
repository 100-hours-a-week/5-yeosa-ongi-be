package ongi.ongibe.domain.ai.repository.queryDSL;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
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

    public int countSuccessStepsByAlbumId(Long albumId) {
        QAiTaskStatus ats = QAiTaskStatus.aiTaskStatus;
        QAiTaskStatus atsSub = new QAiTaskStatus("atsSub");

        var query = queryFactory
                .selectFrom(ats)
                .where(
                        ats.albumId.eq(albumId),
                        ats.status.eq(AiStatus.SUCCESS),
                        ats.createdAt.in(
                                JPAExpressions
                                        .select(atsSub.createdAt.max())
                                        .from(atsSub)
                                        .where(atsSub.albumId.eq(albumId))
                                        .groupBy(atsSub.step)
                        )
                );

        System.out.println("[DEBUG] JPQL: " + query.toString()); // JPQL 확인용

        List<AiTaskStatus> latestSuccessTasks = query.fetch();

        return (int) latestSuccessTasks.stream()
                .map(AiTaskStatus::getStep)
                .distinct()
                .count();
    }
}
