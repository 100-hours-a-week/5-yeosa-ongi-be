package ongi.ongibe.domain.loadtest;

import jakarta.annotation.PostConstruct;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Profile("local")
@RestController
@RequestMapping("/api/testauth")
@RequiredArgsConstructor
public class LoadTestAuthController {

    private final LoadTestAuthService loadTestAuthService;

    @PostConstruct
    public void init() {
        System.out.println("✅ LoadTestAuthController is active!");
    }

    @PostMapping
    public String issueToken() {
        String nickname = "u_" + UUID.randomUUID().toString().substring(0, 8);
        return loadTestAuthService.getOrCreateAccessToken(nickname);
    }
}