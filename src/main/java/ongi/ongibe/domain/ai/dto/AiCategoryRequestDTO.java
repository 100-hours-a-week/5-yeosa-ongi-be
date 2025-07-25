package ongi.ongibe.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record AiCategoryRequestDTO(
        @Schema(description = "이미지 keys") List<String> images,
        @Schema(description = "컨셉 리스트") List<String> concepts
) {}
