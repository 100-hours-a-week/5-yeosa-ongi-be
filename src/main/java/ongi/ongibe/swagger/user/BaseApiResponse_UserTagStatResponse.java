package ongi.ongibe.swagger.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.user.dto.UserTagStatResponseDTO;

@Getter
@Schema(name = "BaseApiResponse_UserTagStatResponse")
public class BaseApiResponse_UserTagStatResponse extends BaseApiResponse<UserTagStatResponseDTO> {}