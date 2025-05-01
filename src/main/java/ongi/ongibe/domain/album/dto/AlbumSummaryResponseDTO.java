package ongi.ongibe.domain.album.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public record AlbumSummaryResponseDTO(
        Long pictureId,
        String pictureURL,
        double latitude,
        double longitude
) {}
