package project.airbnb.clone.config.security.jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import project.airbnb.clone.common.exceptions.ErrorCode;
import project.airbnb.clone.common.exceptions.JwtProcessingException;
import project.airbnb.clone.common.jwt.JwtProvider;
import project.airbnb.clone.service.jwt.TokenService;

import java.io.IOException;

import static project.airbnb.clone.common.jwt.JwtProperties.AUTHORIZATION_HEADER;
import static project.airbnb.clone.common.jwt.JwtProperties.TOKEN_PREFIX;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final TokenService tokenService;

    /**
     * @throws CredentialsExpiredException token has expired
     * @throws InsufficientAuthenticationException token is invalid
     * @throws JwtException cause is EntityNotFoundException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = resolveToken(request);

        if (accessToken != null) {
            if (tokenService.containsBlackList(accessToken)) {
                throw new JwtProcessingException(ErrorCode.BLACKLISTED_TOKEN);
            }

            jwtProvider.validateToken(accessToken);
            SecurityContextHolder.getContext().setAuthentication(jwtProvider.getAuthentication(accessToken));
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return PatternMatchUtils.simpleMatch("/api/auth/logout", requestURI);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith(TOKEN_PREFIX)) {
            return null;
        }

        return bearerToken.substring(TOKEN_PREFIX.length());
    }
}
