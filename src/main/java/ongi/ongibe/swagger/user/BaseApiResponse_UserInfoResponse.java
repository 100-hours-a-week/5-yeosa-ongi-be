package ongi.ongibe.swagger.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.user.dto.UserInfoResponseDTO;

@Getter
@Schema(name = "BaseApiResponse_UserInfoResponse")
public class BaseApiResponse_UserInfoResponse extends
        BaseApiResponse<UserInfoResponseDTO> {}
