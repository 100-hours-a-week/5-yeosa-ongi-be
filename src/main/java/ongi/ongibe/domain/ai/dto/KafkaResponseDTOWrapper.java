package ongi.ongibe.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;

public record KafkaResponseDTOWrapper<T>(
        @Schema(description = "작업 식별자") String taskId,
        @Schema(description = "앨범 아이디") Long albumId,
        @Schema(description = "상태 코드") int statusCode,
        @Schema(description = "기존 사용하던 ai 통신용 DTO") T body
) {}
