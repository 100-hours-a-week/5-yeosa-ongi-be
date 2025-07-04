package ongi.ongibe.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record UserPlaceStatResponseDTO(
        @Schema(description = "시 또는 도") String city,
        @Schema(description = "구") String district,
        @Schema(description = "동") String town,
        @Schema(description = "해당 지역에서의 태그와 태그가 기록된 숫자") List<TagCountDTO> tags
) {
    public record TagCountDTO(String tag, int count) {}
}
