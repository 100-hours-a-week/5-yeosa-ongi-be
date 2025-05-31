package ongi.ongibe.loadtest;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Profile("loadtest")
@RestController
@RequestMapping("/api/test-auth")
@RequiredArgsConstructor
public class LoadTestAuthController {

    private final LoadTestAuthService loadTestAuthService;

    @PostMapping
    public String issueToken(@RequestParam String nickname) {
        return loadTestAuthService.getOrCreateAccessToken(nickname);
    }
}