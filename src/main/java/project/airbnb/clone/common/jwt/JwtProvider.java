package project.airbnb.clone.common.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import project.airbnb.clone.common.exceptions.ErrorCode;
import project.airbnb.clone.common.exceptions.JwtProcessingException;
import project.airbnb.clone.config.security.jwt.JwtAuthenticationToken;
import project.airbnb.clone.entity.Member;
import project.airbnb.clone.model.AuthProviderUser;
import project.airbnb.clone.model.PrincipalUser;
import project.airbnb.clone.repository.jpa.MemberRepository;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

import static io.jsonwebtoken.io.Decoders.BASE64;
import static project.airbnb.clone.common.jwt.JwtProperties.PRINCIPAL_NAME;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final JwtProperties jwtProperties;
    private final MemberRepository memberRepository;

    public JwtProvider(MemberRepository memberRepository, JwtProperties jwtProperties) {
        this.memberRepository = memberRepository;
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(BASE64.decode(jwtProperties.getSecretKey()));
    }

    public String generateAccessToken(Member member, String principalName) {
        return generateToken(member.getId(), jwtProperties.getAccessToken().getExpiration(), principalName);
    }

    public String generateRefreshToken(Member member, String principalName) {
        return generateToken(member.getId(), jwtProperties.getRefreshToken().getExpiration(), principalName);
    }

    public Authentication getAuthentication(String token) {
        Long id = getId(token);
        String principalName = getPrincipalName(token);

        Member member = memberRepository.findById(id)
                                        .orElseThrow(() -> new JwtProcessingException(ErrorCode.MEMBER_NOT_FOUND));
        PrincipalUser principal = new PrincipalUser(new AuthProviderUser(member, principalName));

        return JwtAuthenticationToken.authenticated(principal, token, principal.getAuthorities());
    }

    public void validateToken(String token) {
        try {
            parseClaims(token);
        }
        catch (ExpiredJwtException e) {
            throw new JwtProcessingException(ErrorCode.TOKEN_EXPIRED, e);
        }
        catch (MalformedJwtException e) {
            throw new JwtProcessingException(ErrorCode.MALFORMED_TOKEN, e);
        }
        catch (IllegalArgumentException | JwtException e) {
            throw new JwtProcessingException(ErrorCode.INVALID_TOKEN, e);
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
                            .id(UUID.randomUUID().toString())
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
