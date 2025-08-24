package project.airbnb.clone.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import project.airbnb.clone.config.security.jwt.JwtAuthenticationToken;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.model.AuthProviderUser;
import project.airbnb.clone.model.PrincipalUser;
import project.airbnb.clone.repository.guest.GuestRepository;

import javax.crypto.SecretKey;
import java.util.Date;

import static io.jsonwebtoken.io.Decoders.BASE64;
import static project.airbnb.clone.common.jwt.JwtProperties.PRINCIPAL_NAME;

@Component
public class JwtProvider {

    private final GuestRepository guestRepository;
    private final JwtProperties jwtProperties;
    private final SecretKey key;

    public JwtProvider(GuestRepository guestRepository, JwtProperties jwtProperties) {
        this.guestRepository = guestRepository;
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(BASE64.decode(jwtProperties.getSecretKey()));
    }

    public String generateAccessToken(Guest guest, String principalName) {
        return generateToken(guest.getId(), jwtProperties.getAccessToken().getExpiration(), principalName);
    }

    public String generateRefreshToken(Guest guest, String principalName) {
        return generateToken(guest.getId(), jwtProperties.getRefreshToken().getExpiration(), principalName);
    }

    public Authentication getAuthentication(String token) {
        Long id = getId(token);
        String principalName = getPrincipalName(token);

        try {
            Guest guest = guestRepository.getGuestById(id);

            PrincipalUser principal = new PrincipalUser(new AuthProviderUser(guest, principalName));
            return JwtAuthenticationToken.authenticated(principal, token, principal.getAuthorities());
        } catch (EntityNotFoundException e) {
            throw new JwtException("Cannot found guest for token subject: " + id, e);
        }
    }

    public void validateToken(String token) {
        try {
            parseClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("Token is invalid", e);
        }
    }

    public Long getId(String token) {
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    public String getPrincipalName(String token) {
        Claims claims = parseClaims(token);
        return claims.get(PRINCIPAL_NAME, String.class);
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                   .verifyWith(key)
                   .build()
                   .parseSignedClaims(token)
                   .getPayload();
    }

    private String generateToken(Long id, int expiration, String principalName) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration * 1000L);
        Claims claims = Jwts.claims()
                            .subject(String.valueOf(id))
                            .add(PRINCIPAL_NAME, principalName) //로그아웃, 연결 끊기 요청에 사용될 사용자 식별값
                            .build();

        return Jwts.builder()
                   .claims(claims)
                   .issuedAt(now)
                   .expiration(exp)
                   .signWith(key)
                   .compact();
    }
}
