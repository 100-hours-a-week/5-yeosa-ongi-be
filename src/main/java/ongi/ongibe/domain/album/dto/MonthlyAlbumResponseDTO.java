package ongi.ongibe.domain.album.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import ongi.ongibe.domain.album.AlbumProcessState;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.global.s3.PresignedUrlService;

public record MonthlyAlbumResponseDTO(
        @Schema(description = "앨범 목록") List<AlbumInfo> albumInfo,
        @Schema(description = "다음 페이지 연월") String nextYearMonth,
        @Schema(description = "다음 페이지 존재 여부") boolean hasNext
) {
    public record AlbumInfo(
            @Schema(description = "앨범 ID") Long albumId,
            @Schema(description = "앨범 이름") String albumName,
            @Schema(description = "썸네일 사진 URL") String thumbnailPictureURL,
            @Schema(description = "썸네일 위도") Double latitude,
            @Schema(description = "썸네일 경도") Double longitude,
            @Schema(description = "앨범 생성 일시") LocalDateTime createdAt,
            @Schema(description = "앨범 멤버들의 프로필 이미지 URL 목록") List<String> memberProfileImageURL,
            @Schema(description = "앨범 AI 분석 관련 상태")AlbumProcessState albumProcessState
            ) {
        public static AlbumInfo of(Album album) {
            return new AlbumInfo(
                    album.getId(),
                    album.getName(),
                    album.getThumbnailPicture().getPictureURL(),
                    album.getThumbnailPicture().getLatitude(),
                    album.getThumbnailPicture().getLongitude(),
                    album.getCreatedAt(),
                    album.getUserAlbums().stream()
                            .map(ua -> ua.getUser().getProfileImage())
                            .toList(),
                    album.getProcessState()
            );
        }
    }
}
