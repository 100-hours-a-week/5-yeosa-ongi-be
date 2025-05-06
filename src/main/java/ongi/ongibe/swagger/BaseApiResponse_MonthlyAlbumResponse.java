package ongi.ongibe.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO;

@Schema(name = "BaseApiResponse_MonthlyAlbumResponse")
public class BaseApiResponse_MonthlyAlbumResponse extends BaseApiResponse<MonthlyAlbumResponseDTO> {}
