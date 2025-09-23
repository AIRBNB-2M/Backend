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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.airbnb.clone.dto.guest.SignupRequestDto;
import project.airbnb.clone.service.guest.GuestService;
import project.airbnb.clone.service.jwt.TokenService;

import static project.airbnb.clone.common.jwt.JwtProperties.AUTHORIZATION_HEADER;
import static project.airbnb.clone.common.jwt.JwtProperties.REFRESH_TOKEN_KEY;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenService tokenService;
    private final GuestService guestService;

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
}
