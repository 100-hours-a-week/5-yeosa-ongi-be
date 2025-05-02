package ongi.ongibe.domain.ai.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.Picture;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAlbumService {

    private final AiClient aiClient;

    public void process(Album album) {
        List<Picture> pictures = album.getPictures();
        List<String> urls = pictures.stream()
                .map(Picture::getPictureURL)
                .toList();

        log.info("[AI] 앨범 {} 에 대한 AI 분석 시작 - 총 {}장", album.getId(), urls.size());

        // 1. 임베딩 요청
        aiClient.requestEmbeddings(urls);
        log.info("[AI] 임베딩 요청 완료");

        // 2. 병렬 요청
        CompletableFuture<Void> quality = CompletableFuture.runAsync(() -> {
            log.info("[AI] 품질 분석 시작");
            aiClient.requestQuality(urls);
            log.info("[AI] 품질 분석 완료");
        });

        CompletableFuture<Void> duplicates = CompletableFuture.runAsync(() -> {
            log.info("[AI] 중복 분석 시작");
            aiClient.requestDuplicates(urls);
            log.info("[AI] 중복 분석 완료");
        });

        CompletableFuture<Void> categories = CompletableFuture.runAsync(() -> {
            log.info("[AI] 카테고리 분석 시작");
            aiClient.requestCategories(urls);
            log.info("[AI] 카테고리 분석 완료");
        });

        CompletableFuture.allOf(quality, duplicates, categories).thenRun(() -> {
            log.info("[AI] 미적 점수 분석 시작");
            aiClient.requestAestheticScore(urls);
            log.info("[AI] 미적 점수 분석 완료");
        }).join();

        log.info("[AI] 앨범 {} 분석 전체 완료", album.getId());
    }
}
