package ongi.ongibe.swagger.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.user.dto.UserPictureStatResponseDTO;

@Getter
@Schema(name = "BaseApiResponse_UserPictureStatResponse")
public class BaseApiResponse_UserPictureStatResponse extends
        BaseApiResponse<UserPictureStatResponseDTO> {}

