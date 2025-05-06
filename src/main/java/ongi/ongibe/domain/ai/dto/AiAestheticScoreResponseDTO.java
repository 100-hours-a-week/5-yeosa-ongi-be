package ongi.ongibe.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record AiAestheticScoreResponseDTO(
        @Schema(description = "응답 메시지") String message,
        @Schema(description = "카테고리별 이미지 품질 점수") List<ScoreCategory> data
) {
    public record ScoreCategory(
            @Schema(description = "카테고리 이름") String category,
            @Schema(description = "이미지 및 점수 목록") List<ScoreEntry> images
    ) {
        public record ScoreEntry(
                @Schema(description = "이미지 URL") String image,
                @Schema(description = "품질 점수") double score
        ) {}
    }
}