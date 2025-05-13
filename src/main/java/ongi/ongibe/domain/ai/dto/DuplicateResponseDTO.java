package ongi.ongibe.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record DuplicateResponseDTO(
        @Schema(description = "응답 메시지") String message,
        @Schema(description = "중복되는 이미지") List<List<String>> data
) {}
