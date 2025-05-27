package ongi.ongibe.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record AiClusterResponseDTO(
        @Schema(description = "응답 메시지") String message,
        @Schema(description = "클러스터링 결과 목록") List<ClusterData> data
) {
    @Schema(description = "단일 클러스터 정보")
    public record ClusterData(
            @Schema(description = "해당 클러스터에 포함된 이미지 URL 목록") List<String> images,

            @Schema(description = "대표 얼굴 정보")
            @JsonProperty("representative_face")
            RepresentativeFace representativeFace
    ) {}

    @Schema(description = "대표 얼굴 정보")
    public record RepresentativeFace(
            @Schema(description = "대표 얼굴이 포함된 이미지 URL") String image,
            @Schema(description = "얼굴 bounding box 좌표 [x1, y1, x2, y2]") List<Integer> bbox
    ) {}
}