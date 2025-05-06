package ongi.ongibe.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.AlbumOwnerTransferResponseDTO;

@Schema(name = "BaseApiResponse_AlbumOwnerTransferResponse")
public class BaseApiResponse_AlbumOwnerTransferResponse extends BaseApiResponse<AlbumOwnerTransferResponseDTO> {}
