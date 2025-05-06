package ongi.ongibe.swagger.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.user.dto.UserPlaceStatResponseDTO;

@Getter
@Schema(name = "BaseApiResponse_UserPlaceStatResponse")
public class BaseApiResponse_UserPlaceStatResponse extends
        BaseApiResponse<UserPlaceStatResponseDTO> {}