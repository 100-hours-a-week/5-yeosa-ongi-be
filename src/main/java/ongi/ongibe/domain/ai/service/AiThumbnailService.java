package ongi.ongibe.domain.ai.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiThumbnailService {

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;

    @Transactional
    public void setThumbnail(Album album, List<String> s3keys) {
        log.info("[AI] 썸네일 지정 시작");
        List<Picture> updatedPictures = pictureRepository.findAllByAlbumIdAndS3KeyIn(album.getId(), s3keys);

        Picture thumbnail = updatedPictures.stream()
                .max((p1, p2) -> Float.compare(p1.getQualityScore(), p2.getQualityScore()))
                .orElseGet(updatedPictures::getFirst);

        album.setThumbnailPicture(thumbnail);
        albumRepository.save(album);
        log.info("[AI] 썸네일 지정 완료");
    }
}
