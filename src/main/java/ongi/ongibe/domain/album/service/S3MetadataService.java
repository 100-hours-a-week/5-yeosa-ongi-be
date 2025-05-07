package ongi.ongibe.domain.album.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.domain.album.dto.GPSCoordinateDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Service
@RequiredArgsConstructor
public class S3MetadataService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public GPSCoordinateDTO extractGPS(String pictureUrl) {
        String key;
        String actualBucket;

        if (pictureUrl.startsWith("s3://")) {
            // s3://bucket-name/key 형식일 경우
            String withoutPrefix = pictureUrl.substring(5); // remove "s3://"
            int slashIndex = withoutPrefix.indexOf("/");
            actualBucket = withoutPrefix.substring(0, slashIndex);
            key = withoutPrefix.substring(slashIndex + 1);
        } else if (pictureUrl.contains(".amazonaws.com/")) {
            // https 형식일 경우
            actualBucket = bucket;
            key = pictureUrl.substring(pictureUrl.indexOf(".amazonaws.com/") + 15);
        } else {
            throw new IllegalArgumentException("지원하지 않는 pictureUrl 형식: " + pictureUrl);
        }

        try (var s3Object = s3Client.getObject(
                GetObjectRequest.builder().bucket(actualBucket).key(key).build())) {
            Metadata metadata = ImageMetadataReader.readMetadata(s3Object);
            GpsDirectory gps = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gps != null && gps.getGeoLocation() != null) {
                var geo = gps.getGeoLocation();
                return new GPSCoordinateDTO(geo.getLatitude(), geo.getLongitude());
            } else {return new GPSCoordinateDTO(null, null);}
        } catch (Exception e) {
            throw new RuntimeException("GPS 추출 실패: " + key, e);
        }
    }

}