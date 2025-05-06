package ongi.ongibe.domain.user.dto;

import java.util.Map;

public record UserPictureStatResponseDTO(
        String yearMonth,
        Map<String, Integer> dailyImageCount
) {}
