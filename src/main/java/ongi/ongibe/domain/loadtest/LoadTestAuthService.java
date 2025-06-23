package ongi.ongibe.domain.loadtest;


import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.domain.auth.OAuthProvider;
import ongi.ongibe.domain.user.entity.User;
import ongi.ongibe.domain.user.repository.UserRepository;
import ongi.ongibe.global.util.JwtTokenProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Profile("local")
@Service
@RequiredArgsConstructor
public class LoadTestAuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public String getOrCreateAccessToken(String nickname) {
        Optional<User> optionalUser = userRepository.findByNickname(nickname);
        User user = optionalUser.orElseGet(() -> userRepository.save(
                User.builder()
                        .provider(OAuthProvider.TEST) // enum에 TEST 값 추가 필요
                        .providerId("test_" + UUID.randomUUID())
                        .nickname(nickname)
                        .email(nickname + "@loadtest.com")
                        .profileImage("https://dummy.image/1.png")
                        .build()
        ));

        return jwtTokenProvider.generateAccessToken(user.getId());
    }
}