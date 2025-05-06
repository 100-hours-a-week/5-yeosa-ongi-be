package ongi.ongibe.swagger.s3;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.global.s3.dto.PresignedUrlResponseDTO;

@Getter
@Schema(name = "BaseApiResponse_PresignedUrlResponseDTO")
public class BaseApiResponse_PresignedUrlResponseDTO extends
        BaseApiResponse<PresignedUrlResponseDTO> {}