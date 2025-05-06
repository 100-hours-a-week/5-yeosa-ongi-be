package ongi.ongibe.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

public record UserPictureStatResponseDTO(
        @Schema(description = "연월") String yearMonth,
        @Schema(description = "일자별 업로드 이미지 수 (yyyy-MM-dd -> count)") Map<String, Integer> dailyImageCount
) {}
