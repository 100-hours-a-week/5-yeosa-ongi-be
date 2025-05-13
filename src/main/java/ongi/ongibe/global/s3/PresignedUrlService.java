package ongi.ongibe.global.s3;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.global.s3.dto.PresignedUrlRequestDTO;
import ongi.ongibe.global.s3.dto.PresignedUrlRequestDTO.PictureInfo;
import ongi.ongibe.global.s3.dto.PresignedUrlResponseDTO;
import ongi.ongibe.global.s3.dto.PresignedUrlResponseDTO.PresignedFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class PresignedUrlService {

    private final S3Presigner presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public BaseApiResponse<PresignedUrlResponseDTO> generatePresignedUrls(PresignedUrlRequestDTO request) {
        List<PresignedFile> result = request.pictures().stream()
                .map(picture -> {
                    String key = picture.pictureName();

                    PutObjectRequest putObjectRequest = getObjectRequest(picture.pictureType(), key);

                    PutObjectPresignRequest presignRequest = getPresignRequest(putObjectRequest);

                    URL presignedUrl = presigner.presignPutObject(presignRequest).url();

                    String pictureUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);

                    return new PresignedUrlResponseDTO.PresignedFile(
                            key, presignedUrl.toString(), pictureUrl
                    );
                })
                .toList();

        return BaseApiResponse.<PresignedUrlResponseDTO>builder()
                .code("PRESIGNED_URL_SUCCESS")
                .message("presigned-url 발급 성공")
                .data(new PresignedUrlResponseDTO(result))
                .build();
    }

    private PutObjectPresignRequest getPresignRequest(
            PutObjectRequest putObjectRequest) {
        return PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofMinutes(10)) // todo : 몇분 설정할지 상의
                .build();
    }

    private PutObjectRequest getObjectRequest(String pictureType, String key) {
        return PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(pictureType)
                .build();
    }
}