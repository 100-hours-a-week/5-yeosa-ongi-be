package ongi.ongibe.domain.user.dto;

import java.util.List;

public record UserTagStatResponseDTO(
        String tag,
        List<String> pictureUrls
) {}
