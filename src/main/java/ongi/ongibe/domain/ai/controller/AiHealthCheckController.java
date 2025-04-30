package ongi.ongibe.domain.ai.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class AiHealthCheckController {

    @Value("${ai.server.base-url}")
    private String AI_SERVER_BASE_URL;

    @GetMapping("/api/ai/ping")
    public ResponseEntity<String> ping() {
        RestTemplate restTemplate = new RestTemplate();
        String aiResponse = restTemplate.getForObject(AI_SERVER_BASE_URL + "/health/info", String.class);
        return ResponseEntity.ok(aiResponse);
    }
}