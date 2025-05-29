package ongi.ongibe.domain.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.album.repository.FaceClusterRepository;
import ongi.ongibe.domain.album.repository.PictureFaceClusterRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiClusterService {

    private final AiClient aiClient;
    private final PictureRepository pictureRepository;
    private final FaceClusterRepository faceClusterRepository;
    private final PictureFaceClusterRepository pictureFaceClusterRepository;


}
