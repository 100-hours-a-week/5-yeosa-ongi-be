package ongi.ongibe.global.s3.dto;

import java.util.List;

public record PresignedUrlResponseDTO(List<PresignedFile> presignedFiles) {
    public record PresignedFile(String pictureName, String presignedUrl, String pictureURL) {}
}