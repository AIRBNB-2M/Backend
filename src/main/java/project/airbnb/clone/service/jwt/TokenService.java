package project.airbnb.clone.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import project.airbnb.clone.common.events.logout.OAuthLogoutEvent;
import project.airbnb.clone.common.exceptions.BusinessException;
import project.airbnb.clone.common.exceptions.ErrorCode;
import project.airbnb.clone.common.exceptions.factory.MemberExceptions;
import project.airbnb.clone.common.jwt.JwtProperties;
import project.airbnb.clone.common.jwt.JwtProvider;
import project.airbnb.clone.dto.jwt.TokenResponse;
import project.airbnb.clone.entity.Member;
import project.airbnb.clone.repository.dto.BlacklistedToken;
import project.airbnb.clone.repository.dto.RefreshToken;
import project.airbnb.clone.repository.jpa.MemberRepository;
import project.airbnb.clone.repository.redis.BlacklistedTokenRepository;
import project.airbnb.clone.repository.redis.RefreshTokenRepository;

import java.time.Duration;
import java.util.Date;

import static project.airbnb.clone.common.jwt.JwtProperties.AUTHORIZATION_HEADER;
import static project.airbnb.clone.common.jwt.JwtProperties.REFRESH_TOKEN_KEY;
import static project.airbnb.clone.common.jwt.JwtProperties.TOKEN_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public TokenResponse generateAndSendToken(String email, String principalName, HttpServletResponse response) {
        Member member = memberRepository.findByEmail(email)
                                        .orElseThrow(() -> MemberExceptions.notFoundByEmail(email));

        return getTokenResponse(response, member, principalName);
    }

    public void refreshAccessToken(String refreshToken, HttpServletResponse response) {
        jwtProvider.validateToken(refreshToken);

        Long id = jwtProvider.getId(refreshToken);

        validateSavedRefreshToken(refreshToken);
        refreshTokenRepository.deleteById(refreshToken);

        String principalName = jwtProvider.getPrincipalName(refreshToken);
        Member member = memberRepository.findById(id)
                                        .orElseThrow(() -> MemberExceptions.notFoundById(id));

        getTokenResponse(response, member, principalName);
    }

    private void validateSavedRefreshToken(String refreshToken) {
        boolean isValid = refreshTokenRepository.findById(refreshToken)
                                                .map(savedRefreshToken -> savedRefreshToken.getToken().equals(refreshToken))
                                                .orElse(false);
        if (!isValid) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    private TokenResponse getTokenResponse(HttpServletResponse response, Member member, String principalName) {
        String accessToken = jwtProvider.generateAccessToken(member, principalName);
        String refreshToken = jwtProvider.generateRefreshToken(member, principalName);

        response.addHeader(AUTHORIZATION_HEADER, TOKEN_PREFIX + accessToken);
        Duration refreshDuration = Duration.ofSeconds(jwtProperties.getRefreshToken().getExpiration());

        refreshTokenRepository.save(RefreshToken.builder()
                                                .token(refreshToken)
                                                .memberId(member.getId())
                                                .ttl(refreshDuration.getSeconds())
                                                .build());

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
        return blacklistedTokenRepository.existsById(token);
    }

    public void logoutProcess(String accessToken, String refreshToken) {
        //액세스 토큰 블랙리스트 처리
        addBlackList(accessToken);

        //리프레시 토큰 제거
        Long id = removeRefreshToken(refreshToken);

        //로그아웃 이벤트 발행
        Member member = memberRepository.findById(id)
                                        .orElseThrow(() -> MemberExceptions.notFoundById(id));
        eventPublisher.publishEvent(new OAuthLogoutEvent(member.getSocialType()));
    }

    private void addBlackList(String accessToken) {
        try {
            accessToken = accessToken.substring(TOKEN_PREFIX.length());

            Date now = new Date();
            Claims claims = jwtProvider.parseClaims(accessToken);
            Date expiration = claims.getExpiration();

            long remain = expiration.getTime() - now.getTime();

            if (remain > 0) {
                blacklistedTokenRepository.save(new BlacklistedToken(accessToken, remain));
                log.debug("블랙리스트에 액세스토큰 저장 : {}", accessToken);
            }
        } catch (ExpiredJwtException ignored) {
        }
    }

    private Long removeRefreshToken(String refreshToken) {
        Long id = jwtProvider.getId(refreshToken);
        refreshTokenRepository.deleteById(refreshToken);
        log.debug("레디스에서 리프레시 토큰 제거 : {}", refreshToken);

        return id;
    }
}
