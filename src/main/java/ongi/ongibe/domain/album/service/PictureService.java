package ongi.ongibe.domain.album.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PictureService {

    private final PictureRepository pictureRepository;

    @Transactional
    public List<Picture> updatePicture(List<Long> pictureIds){
        List<Picture> pictures = pictureRepository.findAllById(pictureIds);
        List<String> urls = pictures.stream()
                .map(Picture::getPictureURL)
                .toList();
        pictureRepository.markPicturesDuplicatedAsStable(urls);
        pictureRepository.markPicturesShakyAsStable(urls);
        return pictures;
    }

}
