package ongi.ongibe.domain.user.controller;

import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.ApiResponse;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO;
import ongi.ongibe.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<UserTotalStateResponseDTO>> getUserTotalState() {
        ApiResponse<UserTotalStateResponseDTO> response = userService.getUserTotalState();
        return ResponseEntity.ok(response);
    }
}
