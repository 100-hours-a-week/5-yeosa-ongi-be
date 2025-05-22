package ongi.ongibe.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ongi.ongibe.domain.album.entity.Picture;

public record AiAestheticScoreRequestDTO(
        @Schema(description = "카테고리별 이미지 리스트") List<Category> categories
) {
    public record Category(
            @Schema(description = "이미지 카테고리 이름") String category,
            @Schema(description = "이미지 URL 목록") List<String> images
    ) {}

    public static AiAestheticScoreRequestDTO from(List<Picture> pictures) {
        Map<String, List<String>> grouped = pictures.stream()
                .filter(p -> p.getTag() != null)
                .collect(Collectors.groupingBy(
                        Picture::getTag,
                        Collectors.mapping(Picture::getS3Key, Collectors.toList())
                ));

        List<Category> categories = grouped.entrySet().stream()
                .map(entry -> new Category(entry.getKey(), entry.getValue()))
                .toList();

        return new AiAestheticScoreRequestDTO(categories);
    }
}
