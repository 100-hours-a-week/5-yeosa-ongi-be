package ongi.ongibe.domain.ai.dto;

import java.util.List;

public record ShakyResponseDTO(
        String message,
        List<String> data
) { }
