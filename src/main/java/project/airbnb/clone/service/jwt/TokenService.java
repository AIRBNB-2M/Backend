package project.airbnb.clone.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import project.airbnb.clone.common.events.logout.OAuthLogoutEvent;
import project.airbnb.clone.common.jwt.JwtProperties;
import project.airbnb.clone.common.jwt.JwtProvider;
import project.airbnb.clone.dto.jwt.TokenResponse;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.repository.jpa.GuestRepository;
import project.airbnb.clone.repository.redis.RedisRepository;

import java.time.Duration;
import java.util.Date;

import static project.airbnb.clone.common.jwt.JwtProperties.AUTHORIZATION_HEADER;
import static project.airbnb.clone.common.jwt.JwtProperties.BLACK_LIST_PREFIX;
import static project.airbnb.clone.common.jwt.JwtProperties.REFRESH_TOKEN_KEY;
import static project.airbnb.clone.common.jwt.JwtProperties.TOKEN_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final GuestRepository guestRepository;
    private final RedisRepository redisRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TokenResponse generateAndSendToken(String email, String principalName, HttpServletResponse response) {
        Guest guest = guestRepository.getGuestByEmail(email);

        return getTokenResponse(response, guest, principalName);
    }

    public void refreshAccessToken(String refreshToken, HttpServletResponse response, HttpServletRequest request) {
        jwtProvider.validateToken(refreshToken);

        Long id = jwtProvider.getId(refreshToken);
        String key = String.valueOf(id);

        String savedRefreshToken = redisRepository.getValue(key);
        request.setAttribute("key", key); //예외 발생 시 Advice에서 처리할 수 있도록 저장

        if (!refreshToken.equals(savedRefreshToken)) {
            throw new JwtException("Refresh Token is invalid: " + refreshToken);
        }

        String principalName = jwtProvider.getPrincipalName(refreshToken);

        Guest guest = guestRepository.getGuestById(id);
        getTokenResponse(response, guest, principalName);
    }

    private TokenResponse getTokenResponse(HttpServletResponse response, Guest guest, String principalName) {
        String accessToken = jwtProvider.generateAccessToken(guest, principalName);
        String refreshToken = jwtProvider.generateRefreshToken(guest, principalName);

        response.addHeader(AUTHORIZATION_HEADER, TOKEN_PREFIX + accessToken);
        Duration refreshDuration = Duration.ofSeconds(jwtProperties.getRefreshToken().getExpiration());

        redisRepository.setValue(String.valueOf(guest.getId()), refreshToken, refreshDuration);

        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_KEY, refreshToken)
                                              .path("/")
                                              .secure(true)
                                              .sameSite("None")
                                              .httpOnly(true)
                                              .maxAge(refreshDuration)
                                              .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return new TokenResponse(accessToken, refreshToken);
    }

    public boolean containsBlackList(String token) {
        String key = BLACK_LIST_PREFIX + token;
        return redisRepository.getValue(key) != null;
    }

    public void logoutProcess(String accessToken, String refreshToken) {
        //액세스 토큰 블랙리스트 처리
        addBlackList(accessToken);

        //리프레시 토큰 제거
        Long id = removeRefreshToken(refreshToken);

        //로그아웃 이벤트 발행
        Guest guest = guestRepository.getGuestById(id);
        eventPublisher.publishEvent(new OAuthLogoutEvent(guest.getSocialType()));
    }

    private void addBlackList(String accessToken) {
        try {
            accessToken = accessToken.substring(TOKEN_PREFIX.length());
            String key = BLACK_LIST_PREFIX + accessToken;

            Date now = new Date();
            Claims claims = jwtProvider.parseClaims(accessToken);
            Date expiration = claims.getExpiration();

            long remain = expiration.getTime() - now.getTime();

            if (remain > 0) {
                redisRepository.setValue(key, "logout", Duration.ofMillis(remain));
                log.debug("Access Token added to blacklist: {}", accessToken);
            }
        } catch (ExpiredJwtException ignored) {
        }
    }

    private Long removeRefreshToken(String refreshToken) {
        Long id = jwtProvider.getId(refreshToken);
        redisRepository.deleteValue(String.valueOf(id));
        log.debug("Refresh Token removed from Redis: {}", refreshToken);

        return id;
    }
}
