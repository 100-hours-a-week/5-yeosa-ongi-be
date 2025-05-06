package ongi.ongibe.swagger.album;

import io.swagger.v3.oas.annotations.media.Schema;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.AlbumInviteResponseDTO;

@Schema(name = "BaseApiResponse_AlbumInviteResponse")
public class BaseApiResponse_AlbumInviteResponse extends BaseApiResponse<AlbumInviteResponseDTO> {}
