package ongi.ongibe.cache.user;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCacheService {

    private final RedisCacheService redisCacheService;
    private final UserRepository userRepository;
    private final UserAlbumRepository userAlbumRepository;
    private final PictureRepository pictureRepository;
    private final PlaceRepository placeRepository;

    private static final Duration TTL = Duration.ofHours(12);

    public UserTotalStateResponseDTO getUserTotalState(Long userId) {
        String key = CacheKeyUtil.key("userTotalState", userId);
        return redisCacheService.get(key, UserTotalStateResponseDTO.class).orElseGet(() -> {
            User user = userRepository.findById(userId).orElseThrow();
            UserTotalStateResponseDTO response = buildUserTotalStateResponse(user);
            redisCacheService.set(key, response, TTL);
            return response;
        });
    }

    private UserTotalStateResponseDTO buildUserTotalStateResponse(User user) {
        List<PictureCoordinate> coordinateList =
                pictureRepository.findAllByUser(user).stream()
                        .map(Picture::toPictureCoordinate)
                        .toList();
        int albumCount = userAlbumRepository.countByUser(user);
        int placeCount = placeRepository.countDistinctByPicturesByUser(user);

        return new UserTotalStateResponseDTO(coordinateList, albumCount, placeCount);
    }

    public void refreshUserTotalState(Long userId) {
        String key = CacheKeyUtil.key("userTotalState", userId);
        User user = userRepository.findById(userId).orElseThrow();
        UserTotalStateResponseDTO response = buildUserTotalStateResponse(user);
        redisCacheService.set(key, response, TTL);
    }
}
