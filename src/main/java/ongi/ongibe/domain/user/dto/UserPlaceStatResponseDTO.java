package ongi.ongibe.domain.user.dto;

import java.util.List;

public record UserPlaceStatResponseDTO(
        String city,
        String district,
        String town,
        List<String> tags
) {}
