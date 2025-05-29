package ongi.ongibe.domain.ai.service;

import static org.apache.commons.lang3.StringUtils.join;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.dto.AiAestheticScoreRequestDTO;
import ongi.ongibe.domain.ai.dto.AiAestheticScoreResponseDTO;
import ongi.ongibe.domain.ai.dto.AiClusterResponseDTO;
import ongi.ongibe.domain.ai.dto.CategoryResponseDTO;
import ongi.ongibe.domain.ai.event.AlbumAiCreateNotificationEvent;
import ongi.ongibe.domain.album.AlbumProcessState;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.FaceCluster;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.entity.PictureFaceCluster;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.FaceClusterRepository;
import ongi.ongibe.domain.album.repository.PictureFaceClusterRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.global.security.util.SecurityUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAlbumService {

    private final AiClient aiClient;
    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final FaceClusterRepository faceClusterRepository;
    private final PictureFaceClusterRepository pictureFaceClusterRepository;
    private final AiEmbeddingService aiEmbeddingService;

    public boolean isAiServerAvailable() {
        return aiClient.isAiServerAvailable();
    }

    @Transactional
    public void process(Album album, List<String> s3keys) {
        Long albumId = album.getId();
        log.info("[AI] 앨범 {} 에 대한 AI 분석 시작 - 총 {}장", albumId, s3keys.size());
        try {
            aiEmbeddingService.requestEmbeddings(s3keys);

            // 2. 병렬 요청
            log.info("[AI] 품질 분석 시작");
            CompletableFuture<List<String>> shakyFuture = CompletableFuture.supplyAsync(
                    () -> aiClient.getShakyKeys(albumId, s3keys));
            log.info("[AI] 품질 분석 완료");

            log.info("[AI] 중복 분석 시작");
            CompletableFuture<List<List<String>>> duplicateFuture = CompletableFuture.supplyAsync(
                    () -> aiClient.getDuplicateGroups(albumId, s3keys));
            log.info("[AI] 중복 분석 완료");

            log.info("[AI] 카테고리 분석 시작");
            CompletableFuture<List<CategoryResponseDTO.CategoryResult>> categoryFuture = CompletableFuture.supplyAsync(
                    () -> aiClient.getCategories(albumId, s3keys));
            log.info("[AI] 카테고리 분석 완료");

            List<String> shakyKeys = shakyFuture.join();
            pictureRepository.markPicturesAsShaky(albumId, shakyKeys);

            List<List<String>> duplicateGroups = duplicateFuture.join();
            pictureRepository.markPicturesAsDuplicated(albumId,
                    duplicateGroups.stream().flatMap(List::stream).toList());

            List<CategoryResponseDTO.CategoryResult> categories = categoryFuture.join();
            for (var category : categories) {
                pictureRepository.updateTag(albumId, category.images(), category.category());
            }

            log.info("[AI] 미적 점수 분석 시작");
            List<Picture> pictures = pictureRepository.findAllByS3KeyIn(shakyKeys);
            List<AiAestheticScoreRequestDTO.Category> categoryDTOs = AiAestheticScoreRequestDTO.from(pictures).categories();
            List<AiAestheticScoreResponseDTO.ScoreCategory> scores = aiClient.getAestheticScore(categoryDTOs);

            for (var category : scores) {
                for (var entry : category.images()) {
                    pictureRepository.updateScore(album.getId(), entry.image(), entry.score());
                }
            }
            log.info("[AI] 미적 점수 분석 완료");
            setThumbnail(album, s3keys);

            log.info("[AI] 클러스터 분석 시작");
            List<String> allKeys = pictureRepository.findAllByAlbum(album).stream()
                    .map(Picture::getS3Key).toList();
            List<AiClusterResponseDTO.ClusterData> clusters = aiClient.getClusters(album.getId(),
                    allKeys);
            log.info("[AI] 클러스터 분석 완료");
            saveCluster(album.getId(), clusters);
            eventPublisher.publishEvent(new AlbumAiCreateNotificationEvent(albumId));

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

    private void setThumbnail(Album album, List<String> keys) {
        List<Picture> updatedPictures = pictureRepository.findAllByAlbumIdAndS3KeyIn(album.getId(),
                keys);

        Picture thumbnail = updatedPictures.stream()
                .max((p1, p2) -> Float.compare(p1.getQualityScore(), p2.getQualityScore()))
                .orElseGet(updatedPictures::getFirst);

        album.setThumbnailPicture(thumbnail);
        albumRepository.save(album);
    }

    private void saveCluster(Long albumId, List<AiClusterResponseDTO.ClusterData> clusters) {
        List<Picture> pictures = pictureRepository.findAllByAlbumId(albumId);
        Map<String, Picture> s3KeyToPicture = pictures.stream()
                .collect(Collectors.toMap(Picture::getS3Key, p -> p));
        int clusterIndex = 1;

        for (AiClusterResponseDTO.ClusterData cluster : clusters) {
            List<String> s3Keys = cluster.images();
            String representativeKey = cluster.representativeFace().image();
            List<Integer> bbox = cluster.representativeFace().bbox();

            Picture representative = s3KeyToPicture.get(representativeKey);
            if (representative == null) {
                log.warn("[AI] 대표 이미지 {}에 해당하는 Picture를 찾을 수 없습니다.", representativeKey);
                continue;
            }

            // FaceCluster 저장
            FaceCluster faceCluster = FaceCluster.builder()
                    .representativePicture(representative)
                    .clusterName("사람-" + clusterIndex++)
                    .bboxX1(bbox.get(0))
                    .bboxY1(bbox.get(1))
                    .bboxX2(bbox.get(2))
                    .bboxY2(bbox.get(3))
                    .build();
            faceClusterRepository.save(faceCluster);

            // PictureFaceCluster 저장
            List<PictureFaceCluster> mappings = s3Keys.stream()
                    .map(s3KeyToPicture::get)
                    .filter(Objects::nonNull)
                    .map(p -> PictureFaceCluster.builder()
                            .picture(p)
                            .faceCluster(faceCluster)
                            .build())
                    .toList();

            pictureFaceClusterRepository.saveAll(mappings);
        }
    }
}

