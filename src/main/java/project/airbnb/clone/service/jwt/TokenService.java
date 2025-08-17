package project.airbnb.clone.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
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
import project.airbnb.clone.repository.redis.RedisRepository;

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
    private final RedisRepository redisRepository;

    public TokenResponse generateAndSendToken(String email, HttpServletResponse response) {
        Guest guest = guestRepository.getGuestByEmail(email);

        return getTokenResponse(response, guest);
    }

    public void refreshAccessToken(String refreshToken, HttpServletResponse response, HttpServletRequest request) {
        jwtProvider.validateToken(refreshToken);

        Claims claims = jwtProvider.parseClaims(refreshToken);
        Long id = Long.valueOf(claims.getSubject());

        Guest guest = guestRepository.getGuestById(id);

        String key = String.valueOf(id);
        String savedRefreshToken = redisRepository.getValue(key);
        request.setAttribute("key", key); //예외 발생 시 Advice에서 처리할 수 있도록 저장

        if (!refreshToken.equals(savedRefreshToken)) {
            throw new JwtException("Refresh Token is invalid: " + refreshToken);
        }

        getTokenResponse(response, guest);
    }

    private TokenResponse getTokenResponse(HttpServletResponse response, Guest guest) {
        String accessToken = jwtProvider.generateAccessToken(guest);
        String refreshToken = jwtProvider.generateRefreshToken(guest);

        response.addHeader(AUTHORIZATION_HEADER, TOKEN_PREFIX + accessToken);
        Duration refreshDuration = Duration.ofSeconds(jwtProperties.getRefreshToken().getExpiration());

        redisRepository.setValue(String.valueOf(guest.getId()), refreshToken, refreshDuration);

        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_KEY, refreshToken)
                                              .path("/")
                                              .httpOnly(true)
                                              .maxAge(refreshDuration)
                                              .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return new TokenResponse(accessToken, refreshToken);
    }
}
