package ongi.ongibe.domain.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Tag(name = "AI 헬스체크 API")
@RestController
public class AiHealthCheckController {

    @Value("${ai.server.base-url}")
    private String AI_SERVER_BASE_URL;

    @Operation(
            summary = "AI 서버 헬스 체크",
            description = "AI 서버가 정상 동작 중인지 확인합니다. 내부적으로 /health/info 엔드포인트를 호출합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI 서버 응답 수신 성공"),
            @ApiResponse(responseCode = "500", description = "AI 서버와의 통신 오류 또는 다운 상태")
    })
    @GetMapping("/api/ai/ping")
    public ResponseEntity<String> ping() {
        RestTemplate restTemplate = new RestTemplate();
        String aiResponse = restTemplate.getForObject(AI_SERVER_BASE_URL + "/health/info", String.class);
        return ResponseEntity.ok(aiResponse);
    }
}
