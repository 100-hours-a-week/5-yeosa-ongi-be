package ongi.ongibe.swagger.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO;

@Getter
@Schema(name = "BaseApiResponse_UserTotalStateResponse")
public class BaseApiResponse_UserTotalStateResponse extends
        BaseApiResponse<UserTotalStateResponseDTO> {}