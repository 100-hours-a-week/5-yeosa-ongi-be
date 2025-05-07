package ongi.ongibe.domain.ai.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAlbumService {

    private final AiClient aiClient;
    private final AlbumRepository albumRepository;

    public void process(List<Picture> pictures) {

        List<String> urls = pictures.stream()
                        .map(Picture::getPictureURL)
                        .toList();

        Long albumId = pictures.get(0).getAlbum().getId();
        log.info("[AI] 앨범 {} 에 대한 AI 분석 시작 - 총 {}장", albumId, urls.size());

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
            setThumbnail(pictures);
        }).join();

        log.info("[AI] 앨범 {} 분석 전체 완료", albumId);
    }

    private void setThumbnail(List<Picture> pictures) {
        Picture thumbnail = pictures.stream()
                .max((p1,p2) -> Float.compare(p1.getQualityScore(), p2.getQualityScore()))
                .orElseGet(pictures::getFirst);
        Album album = thumbnail.getAlbum();
        album.setThumbnailPicture(thumbnail);
        albumRepository.save(album);
    }
}
