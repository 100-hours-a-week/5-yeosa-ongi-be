package ongi.ongibe.domain.album.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AlbumProcessService {

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final S3MetadataService s3MetadataService;

    @Async
    @Transactional
    public void processAlbumAsync(Long albumId){
        Album album = albumRepository.findById(albumId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "앨범 없음")
                );
        List<Picture> pictures = album.getPictures();

    }
}
