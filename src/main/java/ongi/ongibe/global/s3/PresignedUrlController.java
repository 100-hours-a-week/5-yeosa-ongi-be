package ongi.ongibe.global.s3;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.global.s3.PresignedUrlService;
import ongi.ongibe.global.s3.dto.PresignedUrlRequestDTO;
import ongi.ongibe.global.s3.dto.PresignedUrlResponseDTO;
import ongi.ongibe.swagger.s3.BaseApiResponse_PresignedUrlResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Presigned URL API", description = "Presigned URL 발급 관련 API입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/presigned-url")
public class PresignedUrlController {

    private final PresignedUrlService presignedUrlService;

    @Operation(summary = "Presigned URL 발급", description = "사진 이름 리스트를 받아 presigned URL을 발급합니다.")
    @ApiResponse(responseCode = "200", description = "URL 발급 성공",
            content = @Content(schema = @Schema(implementation = BaseApiResponse_PresignedUrlResponseDTO.class)))
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseApiResponse<PresignedUrlResponseDTO>> createPresignedUrls(
            @RequestBody PresignedUrlRequestDTO request
    ) {
        BaseApiResponse<PresignedUrlResponseDTO> response = presignedUrlService.generatePresignedUrls(request);
        return ResponseEntity.ok(response);
    }
}
