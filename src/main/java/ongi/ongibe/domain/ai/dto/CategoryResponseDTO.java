package ongi.ongibe.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record CategoryResponseDTO(
        @Schema(description = "응답 메시지") String message,
        @Schema(description = "카테고리별 이미지 목록") List<CategoryResult> data
) {
    public record CategoryResult(
            @Schema(description = "카테고리 이름") String category,
            @Schema(description = "해당 카테고리에 속한 이미지 URL들") List<String> images
    ) {}
}