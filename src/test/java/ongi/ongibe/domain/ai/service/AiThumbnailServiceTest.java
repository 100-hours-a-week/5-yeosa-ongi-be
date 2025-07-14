package ongi.ongibe.domain.ai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiThumbnailServiceTest {

    @InjectMocks
    private AiThumbnailService aiThumbnailService;

    @Mock private AlbumRepository albumRepository;
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
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));

        key1 = "s3://img1.jpg";
        key2 = "s3://img2.jpg";
        key3 = "s3://img3.jpg";
        s3keys = List.of(key1, key2, key3);
        pic1 = mock(Picture.class);
        pic2 = mock(Picture.class);
        pic3 = mock(Picture.class);
    }

    @Test
    void 썸네일지정_score있음(){
        //given
        when(pictureRepository.findAllByAlbumIdAndS3KeyIn(album.getId(), s3keys)).thenReturn(List.of(pic1, pic2, pic3));
        when(pic1.getQualityScore()).thenReturn(1.00f);
        when(pic2.getQualityScore()).thenReturn(2.00f);
        when(pic3.getQualityScore()).thenReturn(3.00f);

        //when
        aiThumbnailService.setThumbnail(album.getId(), s3keys);

        //then
        verify(album).setThumbnailPicture(pic3);
        verify(albumRepository).save(album);
    }
}