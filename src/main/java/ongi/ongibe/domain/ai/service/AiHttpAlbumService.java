package ongi.ongibe.domain.ai.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.aiInterface.AiAlbumServiceInterface;
import ongi.ongibe.domain.ai.event.AlbumAiCreateNotificationEvent;
import ongi.ongibe.domain.album.AlbumProcessState;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
@RequiredArgsConstructor
@Slf4j
public class AiHttpAlbumService implements AiAlbumServiceInterface {

    private final AiClient aiClient;
    private final AlbumRepository albumRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AiHttpEmbeddingService aiHttpEmbeddingService;
    private final AiHttpShakeDuplicateCategoryService aiHttpShakeDuplicateCategoryService;
    private final AiHttpAestheticScoreService aiHttpAestheticScoreService;
    private final AiThumbnailService aiThumbnailService;
    private final AiHttpClusterService aiHttpClusterService;

    public boolean isAiServerAvailable() {
        return aiClient.isAiServerAvailable();
    }

    @Override
    public void process(Album album, Long userId, List<String> s3keys) {
        Long albumId = album.getId();
        log.info("[AI] 앨범 {} 에 대한 AI 분석 시작 - 총 {}장", albumId, s3keys.size());
        try {
            // 0. 헬스체크
            boolean available = aiClient.isAiServerAvailable();
            log.info("[AI] 헬스체크 결과: {}", available);
            if (!available) {
                throw new IllegalStateException("AI 서버 헬스체크 실패");
            }

            // 1. 임베딩
            aiHttpEmbeddingService.requestEmbeddings(albumId, userId, s3keys);

            // 2. 병렬 요청
            aiHttpShakeDuplicateCategoryService.analyzeShakyDuplicateCategory(albumId, userId, s3keys);

            // 3. quality score
            aiHttpAestheticScoreService.requestAestheticScores(albumId, userId, s3keys);

            // 4. quality score 기반 썸네일 지정
            aiThumbnailService.setThumbnail(album, s3keys);

            // 5. 클러스터 분석
            aiHttpClusterService.requestCluster(albumId, userId, s3keys);
            eventPublisher.publishEvent(new AlbumAiCreateNotificationEvent(albumId));

            album.setProcessState(AlbumProcessState.DONE);
            albumRepository.save(album);
            log.info("[AI] 앨범 {} 분석 전체 완료", albumId);
        } catch (Exception e) {
            log.error("[AI 분석 실패] albumId: {}, message: {}", albumId, e.getMessage(), e);
            album.setProcessState(AlbumProcessState.FAILED);
            albumRepository.save(album);
            throw new RuntimeException(e);
        }
    }
}

