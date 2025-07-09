package ongi.ongibe.domain.ai.dto;

import java.util.List;

public record AiErrorResponseDTO(
        String message, List<String> data
) {

}
