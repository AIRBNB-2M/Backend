package project.airbnb.clone.service.jwt;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import project.airbnb.clone.common.jwt.JwtProperties;
import project.airbnb.clone.common.jwt.JwtProvider;
import project.airbnb.clone.dto.jwt.TokenResponse;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.repository.guest.GuestRepository;

import java.time.Duration;

import static project.airbnb.clone.common.jwt.JwtProperties.AUTHORIZATION_HEADER;
import static project.airbnb.clone.common.jwt.JwtProperties.REFRESH_TOKEN_KEY;
import static project.airbnb.clone.common.jwt.JwtProperties.TOKEN_PREFIX;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final GuestRepository guestRepository;

    public TokenResponse generateAndSendToken(String email, HttpServletResponse response) {
        Guest guest = guestRepository.getGuestByEmail(email);

        String accessToken = jwtProvider.generateAccessToken(guest);
        String refreshToken = jwtProvider.generateRefreshToken(guest);

        response.addHeader(AUTHORIZATION_HEADER, TOKEN_PREFIX + accessToken);
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_KEY, refreshToken)
                                              .path("/")
                                              .httpOnly(true)
                                              .maxAge(Duration.ofSeconds(jwtProperties.getRefreshToken().getExpiration()))
                                              .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return new TokenResponse(accessToken, refreshToken);
    }
}
