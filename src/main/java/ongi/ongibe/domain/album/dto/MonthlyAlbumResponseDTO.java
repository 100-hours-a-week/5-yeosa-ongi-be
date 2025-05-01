package ongi.ongibe.domain.album.dto;

import java.time.LocalDateTime;
import java.util.List;
import ongi.ongibe.domain.album.entity.Album;

public record MonthlyAlbumResponseDTO(
        List<AlbumInfo> albumInfo,
        String nextYearMonth,
        boolean hasNext
) {
    public record AlbumInfo(
            Long albumId,
            String albumName,
            String thumbnailPictureURL,
            LocalDateTime createdAt,
            List<String> memberProfileImageURL
    ) {
        public static AlbumInfo of(Album album) {
            return new AlbumInfo(
                    album.getId(),
                    album.getName(),
                    album.getThumbnailPicture() != null ? album.getThumbnailPicture().getPictureURL() : null,
                    album.getCreatedAt(),
                    album.getUserAlbums().stream()
                            .map(ua -> ua.getUser().getProfileImage())
                            .toList()
            );
        }
    }
}
