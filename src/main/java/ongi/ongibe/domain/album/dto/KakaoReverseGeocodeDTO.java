package ongi.ongibe.domain.album.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record KakaoReverseGeocodeDTO(
        @Schema(description = "카카오 응답 관련 정보") Meta meta,
        @Schema(description = "응답 결과") List<Document> documents
) {
    public record Meta(
            @Schema(description = "검색된 문서 수") int count
    ){}

    public record Document(
            @Schema(description = "행정동 또는 법정동(우리는 법정동 사용)") String region_type,
            @Schema(description = "전체 지역 명칭") String address_name,
            @Schema(description = "시도 단위") String region_1depth_name,
            @Schema(description = "구 단위") String region_2depth_name,
            @Schema(description = "동 단위") String region_3depth_name,
            @Schema(description = "리 단위") String region_4depth_name,
            @Schema(description = "region code") String code,
            @Schema(description = "경도") double x,
            @Schema(description = "위도") double y
    ){}
}
