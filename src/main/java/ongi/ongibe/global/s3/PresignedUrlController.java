package ongi.ongibe.global.s3;

import lombok.RequiredArgsConstructor;
import ongi.ongibe.global.s3.dto.PresignedUrlRequest;
import ongi.ongibe.global.s3.dto.PresignedUrlResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/presigned-url")
public class PresignedUrlController {

    private final PresignedUrlService presignedUrlService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PresignedUrlResponse> createPresignedUrls(
            @RequestBody PresignedUrlRequest request
    ) {
        PresignedUrlResponse response = presignedUrlService.generatePresignedUrls(request);
        return ResponseEntity.ok(response);
    }
}
