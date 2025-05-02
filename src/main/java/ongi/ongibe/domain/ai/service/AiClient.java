package ongi.ongibe.domain.ai.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.domain.ai.dto.AiAestheticScoreRequestDTO;
import ongi.ongibe.domain.ai.dto.AiAestheticScoreResponseDTO;
import ongi.ongibe.domain.ai.dto.AiImageRequestDTO;
import ongi.ongibe.domain.ai.dto.CategoryResponseDTO;
import ongi.ongibe.domain.ai.dto.DuplicateResponseDTO;
import ongi.ongibe.domain.ai.dto.ShakyResponseDTO;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AiClient {

    private final RestTemplate restTemplate;
    private final PictureRepository pictureRepository;

    @Value("${ai.server.base-url}")
    private String baseUrl;

    public void requestEmbeddings(List<String> urls) {
        String url = baseUrl + "/api/albums/embeddings";
        restTemplate.postForEntity(url, new AiImageRequestDTO(urls), Void.class);
    }

    public void requestQuality(List<String> urls) {
        String url = baseUrl + "/api/albums/quality";
        List<Picture> pictures = pictureRepository.findAllByPictureURLIn(urls);
        var response = restTemplate.postForObject(url, new AiImageRequestDTO(urls), ShakyResponseDTO.class);
        if (response == null || response.data() == null) return;

        Map<String, Picture> map = toMap(pictures);
        response.data().forEach(urlStr -> {
            Picture p = map.get(urlStr);
            if (p != null) p.markAsShaky();
        });
        pictureRepository.saveAll(pictures);
    }

    public void requestDuplicates(List<String> urls) {
        String url = baseUrl + "/api/albums/duplicates";
        List<Picture> pictures = pictureRepository.findAllByPictureURLIn(urls);
        var response = restTemplate.postForObject(url, new AiImageRequestDTO(urls), DuplicateResponseDTO.class);
        if (response == null || response.data() == null) return;

        Map<String, Picture> map = toMap(pictures);
        response.data().stream()
                .flatMap(List::stream)
                .distinct()
                .forEach(urlStr -> {
                    Picture p = map.get(urlStr);
                    if (p != null) p.markAsDuplicate();
                });
        pictureRepository.saveAll(pictures);
    }

    public void requestCategories(List<String> urls) {
        String url = baseUrl + "/api/albums/categories";
        List<Picture> pictures = pictureRepository.findAllByPictureURLIn(urls);
        var response = restTemplate.postForObject(url, new AiImageRequestDTO(urls), CategoryResponseDTO.class);
        if (response == null || response.data() == null) return;

        Map<String, Picture> map = toMap(pictures);
        for (var category : response.data()) {
            for (String urlStr : category.images()) {
                Picture p = map.get(urlStr);
                if (p != null) p.setTagIfAbsent(category.category());
            }
        }
        pictureRepository.saveAll(pictures);
    }

    public void requestAestheticScore(List<String> urls) {
        String url = baseUrl + "/api/albums/scores";
        List<Picture> pictures = pictureRepository.findAllByPictureURLIn(urls);
        AiAestheticScoreRequestDTO request = AiAestheticScoreRequestDTO.from(pictures);
        var response = restTemplate.postForObject(url, request, AiAestheticScoreResponseDTO.class);
        if (response == null || response.data() == null) return;

        Map<String, Picture> map = toMap(pictures);
        for (var category : response.data()) {
            for (var entry : category.images()) {
                Picture p = map.get(entry.image());
                if (p != null) {
                    p.applyAestheticScore(entry.score());
                    p.setTagIfAbsent(category.category());
                }
            }
        }
        pictureRepository.saveAll(pictures);
    }

    private Map<String, Picture> toMap(List<Picture> pictures) {
        return pictures.stream().collect(Collectors.toMap(Picture::getPictureURL, p -> p));
    }
}
