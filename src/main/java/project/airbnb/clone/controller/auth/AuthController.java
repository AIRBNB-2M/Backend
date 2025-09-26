package project.airbnb.clone.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.airbnb.clone.common.annotations.CurrentGuestId;
import project.airbnb.clone.dto.guest.SignupRequestDto;
import project.airbnb.clone.service.guest.EmailVerificationService;
import project.airbnb.clone.service.guest.GuestService;
import project.airbnb.clone.service.jwt.TokenService;

import java.net.URI;

import static project.airbnb.clone.common.jwt.JwtProperties.AUTHORIZATION_HEADER;
import static project.airbnb.clone.common.jwt.JwtProperties.REFRESH_TOKEN_KEY;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenService tokenService;
    private final GuestService guestService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/refresh")
    public void refreshAccessToken(@CookieValue(value = REFRESH_TOKEN_KEY, required = false) String refreshToken,
                                   HttpServletResponse response, HttpServletRequest request) {
        log.debug("RefreshTokenController.refreshAccessToken");
        tokenService.refreshAccessToken(refreshToken, response, request);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader(value = AUTHORIZATION_HEADER, required = false) String accessToken,
                       @CookieValue(value = REFRESH_TOKEN_KEY, required = false) String refreshToken,
                       HttpServletResponse response) {
        tokenService.logoutProcess(accessToken, refreshToken);

        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_KEY, "")
                                              .path("/")
                                              .httpOnly(true)
                                              .maxAge(0)
                                              .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        guestService.register(signupRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/email/verify")
    public ResponseEntity<?> sendEmail(@CurrentGuestId Long guestId) {
        emailVerificationService.sendEmail(guestId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/email/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        boolean success = emailVerificationService.verifyToken(token);

        //TODO : 프론트엔드 배포 주소 연결
        String redirectUrl = success
                ? "http://localhost:3000/users/profile?emailVerify=success"
                : "http://localhost:3000/users/profile?emailVerify=failed";

        return ResponseEntity.status(HttpStatus.FOUND)
                             .location(URI.create(redirectUrl))
                             .build();
    }
}
