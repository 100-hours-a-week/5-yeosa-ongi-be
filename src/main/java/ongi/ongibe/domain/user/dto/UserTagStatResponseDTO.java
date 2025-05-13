package ongi.ongibe.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record UserTagStatResponseDTO(
        @Schema(description = "태그") String tag,
        @Schema(description = "해당 태그의 사진들") List<String> pictureUrls
) {}
