package ongi.ongibe.domain.place.service;

import lombok.RequiredArgsConstructor;
import ongi.ongibe.domain.album.dto.KakaoAddressDTO;
import ongi.ongibe.domain.place.entity.Place;
import ongi.ongibe.domain.album.repository.PlaceRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;

    public Place findOrCreate(KakaoAddressDTO address) {
        return placeRepository
                .findByCityAndDistrictAndTown(address.city(), address.district(), address.town())
                .orElseGet(() -> {
                    Place newPlace = Place.builder()
                            .city(address.city())
                            .district(address.district())
                            .town(address.town())
                            .build();
                    return placeRepository.save(newPlace);
                });
    }
}

