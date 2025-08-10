package project.airbnb.clone.controller.guets;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.airbnb.clone.dto.guest.SignupRequestDto;
import project.airbnb.clone.service.guest.GuestService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GuestController {

    private final GuestService guestService;

    /**
     * REST 회원가입
     */
    @PostMapping("/auth/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        guestService.register(signupRequestDto);
        return ResponseEntity.ok().build();
    }
}
