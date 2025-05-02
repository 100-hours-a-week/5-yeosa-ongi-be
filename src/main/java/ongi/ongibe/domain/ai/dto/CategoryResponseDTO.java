package ongi.ongibe.domain.ai.dto;

import java.util.List;

public record CategoryResponseDTO(
        String message,
        List<CategoryResult> data
) {
    public record CategoryResult(
            String category,
            List<String> images
    ) {}
}
