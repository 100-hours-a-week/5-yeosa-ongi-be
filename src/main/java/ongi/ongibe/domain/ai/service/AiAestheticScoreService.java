package ongi.ongibe.domain.ai.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.dto.AiAestheticScoreRequestDTO;
import ongi.ongibe.domain.ai.dto.AiAestheticScoreResponseDTO;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class AiAestheticScoreService {

    private final AiClient aiClient;
    private final PictureRepository pictureRepository;

    @Transactional
    public void requestAestheticScores(Album album, List<String> s3keys) {
        log.info("[AI] 미적 점수 분석 시작");
        List<Picture> pictures = pictureRepository.findAllByS3KeyIn(s3keys);
        List<AiAestheticScoreRequestDTO.Category> categories = AiAestheticScoreRequestDTO.from(pictures).categories();
        List<AiAestheticScoreResponseDTO.ScoreCategory> scores = aiClient.getAestheticScore(categories);

        for (var category : scores) {
            for (var entry : category.images()) {
                pictureRepository.updateScore(album.getId(), entry.image(), entry.score());
            }
        }
        log.info("[AI] 미적 점수 분석 완료");
    }

}
