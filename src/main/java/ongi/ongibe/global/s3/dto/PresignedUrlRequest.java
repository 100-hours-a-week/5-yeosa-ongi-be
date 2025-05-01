package ongi.ongibe.global.s3.dto;

import java.util.List;

public record PresignedUrlRequest(List<PictureInfo> pictures) {
    public record PictureInfo(String pictureName, String pictureType) {}
}
