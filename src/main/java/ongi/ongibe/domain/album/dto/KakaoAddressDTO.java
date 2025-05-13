package ongi.ongibe.domain.album.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record KakaoAddressDTO(
        @Schema(description = "시 또는 도") String city,
        @Schema(description = "구") String district,
        @Schema(description = "동") String town
){}
