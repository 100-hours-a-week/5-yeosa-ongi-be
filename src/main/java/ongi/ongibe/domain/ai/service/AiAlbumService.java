package ongi.ongibe.domain.ai.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.event.AlbumAiCreateNotificationEvent;
import ongi.ongibe.domain.album.AlbumProcessState;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.global.security.util.SecurityUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAlbumService {

    private final AiClient aiClient;
    private final AsyncAiClient asyncAiClient;
    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserAlbumRepository userAlbumRepository;
    private final SecurityUtil securityUtil;

    public void process(Album album, List<Picture> pictures) {
        Long albumId = album.getId();
        try{
            List<String> s3keys = pictures.stream()
                    .map(Picture::getS3Key)
                    .toList();

            log.info("[AI] 앨범 {} 에 대한 AI 분석 시작 - 총 {}장", albumId, s3keys.size());
            log.info("[AI] s3keys: {}", s3keys);

            // 1. 임베딩 요청
            aiClient.requestEmbeddings(s3keys);
            log.info("[AI] 임베딩 요청 완료");

            // 2. 병렬 요청
            CompletableFuture<Void> quality = CompletableFuture.runAsync(() -> {
                log.info("[AI] 품질 분석 시작");
                asyncAiClient.requestQuality(albumId, s3keys);
                log.info("[AI] 품질 분석 완료");
            });

            CompletableFuture<Void> duplicates = CompletableFuture.runAsync(() -> {
                log.info("[AI] 중복 분석 시작");
                asyncAiClient.requestDuplicates(albumId, s3keys);
                log.info("[AI] 중복 분석 완료");
            });

            CompletableFuture<Void> categories = CompletableFuture.runAsync(() -> {
                log.info("[AI] 카테고리 분석 시작");
                aiClient.requestCategories(albumId, s3keys);
                log.info("[AI] 카테고리 분석 완료");
            });

            CompletableFuture.allOf(quality, duplicates, categories).thenRun(() -> {
                log.info("[AI] 미적 점수 분석 시작");
                aiClient.requestAestheticScore(albumId, s3keys);
                log.info("[AI] 미적 점수 분석 완료");
                setThumbnail(album, pictures);
            }).join();
            eventPublisher.publishEvent(new AlbumAiCreateNotificationEvent(albumId, securityUtil.getCurrentUserId()));

            log.info("[AI] 앨범 {} 분석 전체 완료", albumId);

            album.setProcessState(AlbumProcessState.DONE);
            albumRepository.save(album);
        } catch (Exception e) {
            log.error("[AI 분석 실패] albumId: {}, message: {}", albumId, e.getMessage(), e);
            album.setProcessState(AlbumProcessState.FAILED);
            albumRepository.save(album);

            throw new RuntimeException(e);
        }
    }


    private void setThumbnail(Album album, List<Picture> pictures) {
        List<String> keys = pictures.stream()
                .map(Picture::getS3Key)
                .toList();

        List<Picture> updatedPictures = pictureRepository.findAllByAlbumIdAndS3KeyIn(album.getId(), keys);

        Picture thumbnail = updatedPictures.stream()
                .max((p1, p2) -> Float.compare(p1.getQualityScore(), p2.getQualityScore()))
                .orElseGet(updatedPictures::getFirst);

        album.setThumbnailPicture(thumbnail);
        albumRepository.save(album);
    }
}
