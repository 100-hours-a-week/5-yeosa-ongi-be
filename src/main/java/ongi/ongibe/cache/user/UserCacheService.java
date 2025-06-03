package ongi.ongibe.cache.user;


import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.album.repository.PlaceRepository;
import ongi.ongibe.domain.album.repository.UserAlbumRepository;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO.PictureCoordinate;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.cache.CacheKeyUtil;
import ongi.ongibe.global.cache.RedisCacheService;
import ongi.ongibe.global.security.util.SecurityUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCacheService {

    private final RedisCacheService redisCacheService;
    private final UserAlbumRepository userAlbumRepository;
    private final PictureRepository pictureRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;

    private static final Duration TTL = Duration.ofHours(8);

    public UserTotalStateResponseDTO getUserTotalState(Long userId) {
        String key = CacheKeyUtil.key("userTotalState", userId);
        return redisCacheService.get(key, UserTotalStateResponseDTO.class).orElseGet(() -> {
            User user = userRepository.findById(userId).orElseThrow();
            List<PictureCoordinate> coordinateList =
                    pictureRepository.findAllByUser(user).stream()
                            .map(Picture::toPictureCoordinate)
                            .toList();
            int albumCount = userAlbumRepository.countByUser(user);
            int placeCount = placeRepository.countDistinctByPicturesByUser(user);

            UserTotalStateResponseDTO response =
                    new UserTotalStateResponseDTO(coordinateList, albumCount, placeCount);
            redisCacheService.set(key, response, TTL);
            return response;
        });
    }
}
