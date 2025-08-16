package project.airbnb.clone.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import project.airbnb.clone.entity.Guest;

import java.security.Key;
import java.util.Date;

import static io.jsonwebtoken.io.Decoders.BASE64;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(BASE64.decode(jwtProperties.getSecretKey()));
    }

    public String generateAccessToken(Guest guest) {
        return generateToken(guest.getId(), jwtProperties.getAccessToken().getExpiration());
    }

    public String generateRefreshToken(Guest guest) {
        return generateToken(guest.getId(), jwtProperties.getRefreshToken().getExpiration());
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
