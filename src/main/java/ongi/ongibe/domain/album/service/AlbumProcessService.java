package ongi.ongibe.domain.album.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.domain.album.dto.KakaoAddressDTO;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.place.entity.Place;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.album.repository.PlaceRepository;
import ongi.ongibe.domain.place.service.PlaceService;
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
    private final KakaoMapService kakaoMapService;
    private final PlaceService placeService;

    @Async
    @Transactional
    public void processAlbumAsync(Long albumId){
        Album album = albumRepository.findById(albumId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "앨범 없음")
                );
        List<Picture> pictures = album.getPictures();

        for (Picture p:pictures){
            var gps = s3MetadataService.extractGPS(p.getPictureURL());
            p.setLatitude(gps.lat());
            p.setLongitude(gps.lon());

            var address = kakaoMapService.reverseGeocode(gps.lat(), gps.lon());
            Place place = placeService.findOrCreate(address);
            p.setPlace(place);
        }
        pictureRepository.saveAll(pictures);
        album.setPictures(pictures);
        albumRepository.save(album);
    }
}
