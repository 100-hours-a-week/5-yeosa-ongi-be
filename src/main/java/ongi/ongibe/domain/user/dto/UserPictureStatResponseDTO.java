package ongi.ongibe.domain.user.dto;

import java.util.Map;

public record UserImageStatResponseDTO(
        String yearMonth,
        Map<String, Integer> dailyImageCount
) {}
