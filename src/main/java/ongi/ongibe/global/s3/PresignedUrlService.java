package ongi.ongibe.global.s3;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.global.s3.dto.PresignedUrlRequestDTO;
import ongi.ongibe.global.s3.dto.PresignedUrlRequestDTO.PictureInfo;
import ongi.ongibe.global.s3.dto.PresignedUrlResponseDTO;
import ongi.ongibe.global.s3.dto.PresignedUrlResponseDTO.PresignedFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
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
                    String type = picture.pictureType();
                    if (!List.of("image/jpg", "image/jpeg", "image/png", "image/webp").contains(type.toLowerCase())) {
                        throw new IllegalArgumentException("지원하지 않는 확장자입니다: " + type);
                    }

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

    public String generatePresignedUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }


    private String extractS3Key(String fullUrl){
        try {
            URI uri = URI.create(fullUrl);
            URL url = uri.toURL();
            String path = url.getPath();

            if (path.startsWith("/" + bucket + "/")) {
                return path.substring(("/" + bucket + "/").length());
            } else {
                return path.substring(1); // "/pictures/a.jpg" → "pictures/a.jpg"
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid S3 URL: " + fullUrl);
        }
    }

    public String generateGetPresignedUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }


    public String extractS3Key(String fullUrl) {
        try {
            URI uri = URI.create(fullUrl);
            String path = uri.getRawPath(); // 예: "/pictures/a.jpg" 또는 "/bucket-name/pictures/a.jpg"
            log.debug("path: {}", path);

            // path 앞에 '/'가 항상 붙기 때문에 제거
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            // 만약 path가 "bucket-name/pictures/a.jpg" 형태면 버킷 제거
            if (path.startsWith(bucket + "/")) {
                path = path.substring((bucket + "/").length());
            }

            return path;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid S3 URL: " + fullUrl, e);
        }
    }
}