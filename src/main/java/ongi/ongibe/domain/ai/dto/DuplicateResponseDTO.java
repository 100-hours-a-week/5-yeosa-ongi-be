package ongi.ongibe.domain.ai.dto;

import java.util.List;

public record DuplicateResponseDTO(
        String message,
        List<List<String>> data
) {}
