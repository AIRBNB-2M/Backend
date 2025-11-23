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
import org.springframework.web.bind.annotation.*;
import project.airbnb.clone.common.annotations.CurrentMemberId;
import project.airbnb.clone.dto.member.SignupRequestDto;
import project.airbnb.clone.service.jwt.TokenService;
import project.airbnb.clone.service.member.EmailVerificationService;
import project.airbnb.clone.service.member.MemberService;

import java.net.URI;

import static project.airbnb.clone.common.jwt.JwtProperties.AUTHORIZATION_HEADER;
import static project.airbnb.clone.common.jwt.JwtProperties.REFRESH_TOKEN_KEY;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenService tokenService;
    private final MemberService memberService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/refresh")
    public void refreshAccessToken(@CookieValue(REFRESH_TOKEN_KEY) String refreshToken,
                                   HttpServletResponse response, HttpServletRequest request) {
        log.debug("RefreshTokenController.refreshAccessToken");
        tokenService.refreshAccessToken(refreshToken, response, request);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader(AUTHORIZATION_HEADER) String accessToken,
                       @CookieValue(REFRESH_TOKEN_KEY) String refreshToken,
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
        memberService.register(signupRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/email/verify")
    public ResponseEntity<?> sendEmail(@CurrentMemberId Long memberId) {
        emailVerificationService.sendEmail(memberId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/email/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        String redirectUrl = emailVerificationService.verifyToken(token);

        return ResponseEntity.status(HttpStatus.FOUND)
                             .location(URI.create(redirectUrl))
                             .build();
    }
}
