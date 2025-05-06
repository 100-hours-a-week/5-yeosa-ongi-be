package ongi.ongibe.domain.user.controller;

import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.user.dto.UserPictureStatResponseDTO;
import ongi.ongibe.domain.user.dto.UserPlaceStatResponseDTO;
import ongi.ongibe.domain.user.dto.UserTagStatResponseDTO;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO;
import ongi.ongibe.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/statistics")
    public ResponseEntity<BaseApiResponse<UserTotalStateResponseDTO>> getUserTotalState() {
        BaseApiResponse<UserTotalStateResponseDTO> response = userService.getUserTotalState();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics/picture")
    public ResponseEntity<BaseApiResponse<UserPictureStatResponseDTO>> getUsePictureStat(@RequestParam String yearMonth) {
        BaseApiResponse<UserPictureStatResponseDTO> response = userService.getUserPictureStat(yearMonth);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics/place")
    public ResponseEntity<BaseApiResponse<UserPlaceStatResponseDTO>> getUserPlaceStat(@RequestParam String yearMonth) {
        BaseApiResponse<UserPlaceStatResponseDTO> response = userService.getUserPlaceStat(yearMonth);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics/tag")
    public ResponseEntity<BaseApiResponse<UserTagStatResponseDTO>> getUserTagStat(@RequestParam String yearMonth) {
        BaseApiResponse<UserTagStatResponseDTO> response = userService.getUserTagStat(yearMonth);
        return ResponseEntity.ok(response);
    }

}
