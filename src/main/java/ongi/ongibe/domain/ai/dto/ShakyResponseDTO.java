package ongi.ongibe.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ShakyResponseDTO(
        @Schema(description = "응답 메시지") String message,
        @Schema(description = "품질 저하 사진 리스트") List<String> data
) { }
