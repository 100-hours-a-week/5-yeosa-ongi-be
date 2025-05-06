package ongi.ongibe.swagger.album;

import io.swagger.v3.oas.annotations.media.Schema;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.AlbumDetailResponseDTO;

@Schema(name = "BaseApiResponse_AlbumDetailResponse")
public class BaseApiResponse_AlbumDetailResponse extends BaseApiResponse<AlbumDetailResponseDTO> {}
