package ongi.ongibe.swagger.album;

import io.swagger.v3.oas.annotations.media.Schema;
import ongi.ongibe.common.BaseApiResponse;

/**
 * BaseApiResponse<String> 의 Swagger 표현용 클래스
 */
@Schema(name = "BaseApiResponse_String", description = "문자열 데이터 응답")
public class BaseApiResponse_String extends BaseApiResponse<String> {}
