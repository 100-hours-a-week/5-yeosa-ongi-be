package ongi.ongibe.domain.ai.service;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import ongi.ongibe.domain.ai.dto.AiAestheticScoreRequestDTO;
import ongi.ongibe.domain.ai.dto.AiAestheticScoreResponseDTO.ScoreCategory;
import ongi.ongibe.domain.ai.dto.AiAestheticScoreResponseDTO.ScoreCategory.ScoreEntry;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiHttpAestheticScoreServiceTest {

    @InjectMocks
    private AiHttpAestheticScoreService aiHttpAestheticScoreService;

    @Mock private AiClient aiClient;
    @Mock private PictureRepository pictureRepository;

    private Album album;
    private Long albumId;
    private List<String> s3keys;
    private String key1, key2, key3;
    private Picture pic1, pic2, pic3;

    @BeforeEach
    void setUp() {
        albumId = 1L;
        album = mock(Album.class);
        when(album.getId()).thenReturn(albumId);

        key1 = "s3://img1.jpg";
        key2 = "s3://img2.jpg";
        key3 = "s3://img3.jpg";
        s3keys = List.of(key1, key2, key3);
        pic1 = mock(Picture.class);
        pic2 = mock(Picture.class);
        pic3 = mock(Picture.class);
    }

    @Test
    void requestAestheticScores_정상동작() {
        // given
        List<Picture> pictures = List.of(pic1, pic2, pic3);
        when(pictureRepository.findAllByS3KeyIn(s3keys)).thenReturn(pictures);

        List<AiAestheticScoreRequestDTO.Category> requestCategories = AiAestheticScoreRequestDTO.from(pictures).categories();

        List<ScoreCategory> scoreCategories = List.of(
                new ScoreCategory("categoryA", List.of(
                        new ScoreEntry(key1, 85.0),
                        new ScoreEntry(key2, 90.0),
                        new ScoreEntry(key3, 95.0)
                ))
        );
        when(aiClient.getAestheticScore(requestCategories)).thenReturn(scoreCategories);

        // when
        aiHttpAestheticScoreService.requestAestheticScores(anyLong(), anyLong(), anyList());

        // then
        verify(pictureRepository).updateScore(albumId, key1, 85.0);
        verify(pictureRepository).updateScore(albumId, key2, 90.0);
        verify(pictureRepository).updateScore(albumId, key3, 95.0);
    }
}