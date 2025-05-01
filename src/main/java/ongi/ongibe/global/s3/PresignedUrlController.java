package ongi.ongibe.global.s3;

import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.global.s3.dto.PresignedUrlRequestDTO;
import ongi.ongibe.global.s3.dto.PresignedUrlResponseDTO;
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
    public ResponseEntity<BaseApiResponse<PresignedUrlResponseDTO>> createPresignedUrls(
            @RequestBody PresignedUrlRequestDTO request
    ) {
        BaseApiResponse<PresignedUrlResponseDTO> response = presignedUrlService.generatePresignedUrls(request);
        return ResponseEntity.ok(response);
    }
}
