package ongi.ongibe.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.AlbumMemberResponseDTO;

@Schema(name = "BaseApiResponse_AlbumMemberResponse")
public class BaseApiResponse_AlbumMemberResponse extends BaseApiResponse<AlbumMemberResponseDTO> {}
