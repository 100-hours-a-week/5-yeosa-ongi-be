package ongi.ongibe.domain.ai.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.dto.CategoryResponseDTO;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiShakeDuplicateCategoryService {

    private final AiClient aiClient;
    private final PictureRepository pictureRepository;

    @Transactional
    public void analyzeShakyDuplicateCategory(Album album, List<String> s3keys) {
        Long albumId = album.getId();
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
    }

}
