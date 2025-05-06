package ongi.ongibe.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.AlbumSummaryResponseDTO;

/**
 * BaseApiResponse<List<AlbumSummaryResponseDTO>> 의 Swagger 표현용 클래스
 */
@Schema(name = "BaseApiResponse_AlbumSummaryResponseList", description = "앨범 요약 리스트 응답")
public class BaseApiResponse_AlbumSummaryResponseList extends BaseApiResponse<List<AlbumSummaryResponseDTO>> {}
