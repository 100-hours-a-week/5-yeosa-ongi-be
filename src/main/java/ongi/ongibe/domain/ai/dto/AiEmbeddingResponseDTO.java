package ongi.ongibe.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import ongi.ongibe.domain.ai.dto.AiClusterResponseDTO.ClusterData;

public record AiEmbeddingResponseDTO(
        @Schema(description = "응답 메시지") String message,
        @Schema(description = "데이터") List<String> data
) {}
