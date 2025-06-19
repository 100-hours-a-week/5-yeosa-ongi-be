package ongi.ongibe.domain.ai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import ongi.ongibe.domain.ai.dto.CategoryResponseDTO;
import ongi.ongibe.domain.ai.dto.CategoryResponseDTO.CategoryResult;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiHttpShakeDuplicateCategoryServiceTest {

    @InjectMocks
    private AiHttpShakeDuplicateCategoryService service;

    @Mock private AiClient aiClient;

    @Mock private PictureRepository pictureRepository;

    private Long albumId;
    private Album album;
    private List<String> s3keys;
    private List<String> shakeKeys;
    private List<List<String>> duplicateKeys;
    private String key1, key2, key3;
    private CategoryResponseDTO.CategoryResult result1, result2;

    @BeforeEach
    void setUp() {
        albumId = 1L;
        album = mock(Album.class);
        when(album.getId()).thenReturn(albumId);
        key1 = "s3://img1.jpg";
        key2 = "s3://img2.jpg";
        key3 = "s3://img3.jpg";
        s3keys = List.of(key1, key2, key3);
        shakeKeys = List.of(key1);
        duplicateKeys = List.of(List.of(key1, key2));

        result1 = new CategoryResult(
                "테스트", List.of(key1, key2)
        );
        result2 = new CategoryResult(
                "테스트2", List.of(key3)
        );

    }

    @Test
    void analyzeShakyDuplicateCategory_정상() {
        //given
        when(aiClient.getShakyKeys(albumId, s3keys)).thenReturn(shakeKeys);
        when(aiClient.getDuplicateGroups(albumId, s3keys)).thenReturn(duplicateKeys);
        when(aiClient.getCategories(albumId, s3keys)).thenReturn(List.of(result1, result2));

        //when
        service.analyzeShakyDuplicateCategory(albumId, 1L, s3keys);

        //then
        verify(pictureRepository).markPicturesAsShaky(albumId, shakeKeys);
        verify(pictureRepository).markPicturesAsDuplicated(albumId, duplicateKeys.stream().flatMap(List::stream).toList());
        verify(pictureRepository).updateTag(eq(albumId), eq(List.of(key1, key2)), eq("테스트"));
        verify(pictureRepository).updateTag(eq(albumId), eq(List.of(key3)), eq("테스트2"));
    }

    @Test
    void analyzeShakyDuplicateCategory_실패시_예외던짐() {
        // given
        when(aiClient.getShakyKeys(albumId, s3keys)).thenThrow(new RuntimeException("AI 통신 실패"));

        // when then
        RuntimeException e = assertThrows(RuntimeException.class, () -> {
            service.analyzeShakyDuplicateCategory(albumId, 1L, s3keys);
        });

        assertTrue(e.getMessage().contains("AI 분석 실패"));
    }
}