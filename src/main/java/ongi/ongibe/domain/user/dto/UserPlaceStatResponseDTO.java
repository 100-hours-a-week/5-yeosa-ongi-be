package ongi.ongibe.domain.user.dto;

import java.util.List;
import java.util.Map;

public record UserPlaceStatResponseDTO(
        String yearMonth,
        Map<String, Integer> dailyImageCount
) {}
