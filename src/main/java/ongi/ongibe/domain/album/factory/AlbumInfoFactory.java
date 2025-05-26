package ongi.ongibe.domain.album.factory;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO.AlbumInfo;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.s3.PresignedUrlService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlbumInfoFactory {

    private final PresignedUrlService presignedUrlService;
    private final UserRepository userRepository;

    public AlbumInfo from(Album album) {
        String thumbnailKey = Optional.ofNullable(album.getThumbnailPicture())
                .map(p -> {
                    if (p.getS3Key() != null) return p.getS3Key();
                    String key = presignedUrlService.extractS3Key(p.getPictureURL());
                    p.setS3Key(key);
                    return key;
                })
                .orElse(null);

        String presignedThumbnailUrl = thumbnailKey != null
                ? presignedUrlService.generateGetPresignedUrl(thumbnailKey)
                : null;

        List<String> memberProfileImageUrls = album.getUserAlbums().stream()
                .map(ua -> {
                    String rawUrl = ua.getUser().getProfileImage();
                    if (rawUrl == null) return null;
                    if (!rawUrl.contains("amazonaws.com")) {
                        return rawUrl;
                    }
                    if (ua.getUser().getS3Key() != null) {
                        return presignedUrlService.generateGetPresignedUrl(ua.getUser().getS3Key());
                    }
                    String key = presignedUrlService.extractS3Key(rawUrl);
                    ua.getUser().setS3Key(key);
                    userRepository.save(ua.getUser());
                    return presignedUrlService.generateGetPresignedUrl(key);
                })
                .toList();

        return new AlbumInfo(
                album.getId(),
                album.getName(),
                presignedThumbnailUrl,
                album.getCreatedAt(),
                memberProfileImageUrls,
                album.getProcessState()
        );
    }
}

