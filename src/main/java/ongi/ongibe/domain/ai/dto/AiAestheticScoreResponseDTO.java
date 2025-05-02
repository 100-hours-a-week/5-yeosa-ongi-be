package ongi.ongibe.domain.ai.dto;

import java.util.List;

public record AiAestheticScoreResponseDTO(
        String message,
        List<ScoreCategory> data
) {
    public record ScoreCategory(String category, List<ScoreEntry> images) {
        public record ScoreEntry(String image, double score) {}
    }
}