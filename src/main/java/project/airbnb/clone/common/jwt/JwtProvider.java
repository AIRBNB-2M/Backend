package project.airbnb.clone.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
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

    public String generateAccessToken(Guest guest) {
        return generateToken(guest.getId(), jwtProperties.getAccessToken().getExpiration());
    }

    public String generateRefreshToken(Guest guest) {
        return generateToken(guest.getId(), jwtProperties.getRefreshToken().getExpiration());
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        Long id = Long.parseLong(claims.getSubject());

        try {
            Guest guest = guestRepository.getGuestById(id);

            PrincipalUser principal = new PrincipalUser(new AuthProviderUser(guest));
            return JwtAuthenticationToken.authenticated(principal, token, principal.getAuthorities());
        } catch (EntityNotFoundException e) {
            throw new JwtException("Cannot found guest for token subject: " + id, e);
        }
    }

    public void validateToken(String token) {
        try {
            parseClaims(token);
        } catch (ExpiredJwtException e) {
            throw new CredentialsExpiredException("The token has expired.", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InsufficientAuthenticationException("The token is invalid.", e);
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                   .verifyWith(key)
                   .build()
                   .parseSignedClaims(token)
                   .getPayload();
    }

    private String generateToken(Long id, int expiration) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration * 1000L);
        Claims claims = Jwts.claims().subject(String.valueOf(id)).build();

        return Jwts.builder()
                   .claims(claims)
                   .issuedAt(now)
                   .expiration(exp)
                   .signWith(key)
                   .compact();
    }
}
