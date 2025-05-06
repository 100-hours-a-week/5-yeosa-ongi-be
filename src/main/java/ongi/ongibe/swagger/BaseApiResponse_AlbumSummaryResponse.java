package ongi.ongibe.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.AlbumSummaryResponseDTO;

@Schema(name = "BaseApiResponse_AlbumSummaryResponse")
public class BaseApiResponse_AlbumSummaryResponse extends BaseApiResponse<AlbumSummaryResponseDTO> {}
