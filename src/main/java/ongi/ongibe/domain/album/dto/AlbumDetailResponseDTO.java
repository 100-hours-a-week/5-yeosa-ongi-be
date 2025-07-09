package ongi.ongibe.domain.album.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import ongi.ongibe.domain.album.AlbumProcessState;

public record AlbumDetailResponseDTO(
        @Schema(description = "앨범 제목") String title,
        @Schema(description = "사진 정보 목록") List<PictureInfo> picture,
        @Schema(description = "앨범 AI 분석 상태") AlbumProcessState albumProcessState,
        @Schema(description = "댓글 수") int commentCount,
        @Schema(description = "사람 클러스터 정보 목록") List<ClusterInfo> cluster
) {
    public record PictureInfo(
            @Schema(description = "사진 ID") Long pictureId,
            @Schema(description = "사진 URL") String pictureURL,
            @Schema(description = "위도") Double latitude,
            @Schema(description = "경도") Double longitude,
            @Schema(description = "태그") String tag,
            @Schema(description = "품질 점수") float qualityScore,
            @Schema(description = "중복 여부") boolean isDuplicated,
            @Schema(description = "흔들림 여부") boolean isShaky,
            @Schema(description = "촬영 시각") LocalDateTime takeAt
    ) {}

    public record ClusterInfo(
            @Schema(description = "클러스터 ID") Long clusterId,
            @Schema(description = "클러스터 이름") String clusterName,
            @Schema(description = "대표 이미지 URL") String representativePicture,
            @Schema(description = "좌표 정보") int bboxX1,
            @Schema(description = "좌표 정보") int bboxY1,
            @Schema(description = "좌표 정보") int bboxX2,
            @Schema(description = "좌표 정보") int bboxY2,
            @Schema(description = "해당 클러스터 이미지 목록") List<String> clusterPicture
    ) {}
}
