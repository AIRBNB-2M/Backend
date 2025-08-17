package project.airbnb.clone.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.airbnb.clone.service.jwt.TokenService;

import static project.airbnb.clone.common.jwt.JwtProperties.REFRESH_TOKEN_KEY;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RefreshTokenController {

    private final TokenService tokenService;

    @PostMapping("/auth/refresh")
    public void refreshAccessToken(
            @CookieValue(value = REFRESH_TOKEN_KEY, required = false) String refreshToken,
            HttpServletResponse response, HttpServletRequest request)
    {
        log.debug("RefreshTokenController.refreshAccessToken");

        tokenService.refreshAccessToken(refreshToken, response, request);
    }
}
