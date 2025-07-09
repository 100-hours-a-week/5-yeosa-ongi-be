package ongi.ongibe.domain.ai.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import ongi.ongibe.domain.ai.dto.AiClusterResponseDTO;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.FaceCluster;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.FaceClusterRepository;
import ongi.ongibe.domain.album.repository.PictureFaceClusterRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiHttpClusterServiceTest {

    @InjectMocks
    private AiHttpClusterService aiHttpClusterService;

    @Mock
    private AiClient aiClient;
    @Mock
    private PictureRepository pictureRepository;
    @Mock
    private FaceClusterRepository faceClusterRepository;
    @Mock
    private PictureFaceClusterRepository pictureFaceClusterRepository;

    private Long albumId;
    private Album album;
    private Picture pic1, pic2;
    private AiClusterResponseDTO.ClusterData clusterData;

    @BeforeEach
    void setUp() {
        albumId = 1L;
        album = mock(Album.class);
        when(album.getId()).thenReturn(albumId);
        pic1 = Picture.builder().id(1L).s3Key("s3://img1.jpg").build();
        pic2 = Picture.builder().id(2L).s3Key("s3://img2.jpg").build();
        when(pictureRepository.findAllByAlbumId(albumId)).thenReturn(List.of(pic1, pic2));

        AiClusterResponseDTO.RepresentativeFace repFace = new AiClusterResponseDTO.RepresentativeFace(
                "s3://img1.jpg", List.of(10, 20, 110, 120)
        );
        clusterData = new AiClusterResponseDTO.ClusterData(
                List.of("s3://img1.jpg", "s3://img2.jpg"), repFace
        );
    }

    @Test
    void requestCluster_정상동작_기존클러스터있음() {
        // given
        when(aiClient.getClusters(eq(albumId), anyList()))
                .thenReturn(List.of(clusterData));
        FaceCluster oldCluster = FaceCluster.builder().id(10L).build();
        when(faceClusterRepository.findAllByAlbumId(albumId)).thenReturn(List.of(oldCluster));

        // when
        aiHttpClusterService.requestCluster(anyLong(), anyLong(), anyList());

        // then
        verify(pictureFaceClusterRepository).deleteAllByFaceClusterIds(any(), eq(List.of(10L)));
        verify(faceClusterRepository).deleteAllByIdInBatch(eq(List.of(10L)));
        verify(faceClusterRepository).save(any(FaceCluster.class));
        verify(pictureFaceClusterRepository).saveAll(anyList());
    }

    @Test
    void requestCluster_정상동작_기존클러스터없음() {
        // given
        when(aiClient.getClusters(eq(albumId), anyList()))
                .thenReturn(List.of(clusterData));
        when(faceClusterRepository.findAllByAlbumId(albumId)).thenReturn(List.of());

        // when
        aiHttpClusterService.requestCluster(anyLong(), anyLong(), anyList());

        // then
        verify(faceClusterRepository).save(any(FaceCluster.class));
        verify(pictureFaceClusterRepository).saveAll(anyList());
    }
}