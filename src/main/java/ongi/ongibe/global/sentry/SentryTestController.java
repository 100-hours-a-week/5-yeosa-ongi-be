package ongi.ongibe.global.sentry;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sentry")
public class SentryTestController {

    @GetMapping("/error")
    public String error() {
        throw new RuntimeException("테스트용 에러입니다! Sentry로 보내볼게요.");
    }
}

