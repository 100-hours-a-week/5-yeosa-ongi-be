package ongi.ongibe.domain.album.dto;

import java.util.List;

public record KakaoReverseGeocodeDTO(
        Meta meta,
        List<Document> documents
) {
    public record Meta(
            int count
    ){}

    public record Document(
            String region_type,
            String address_name,
            String region_1depth_name,
            String region_2depth_name,
            String region_3depth_name,
            String region_4depth_name,
            String code,
            double x,
            double y
    ){}
}
