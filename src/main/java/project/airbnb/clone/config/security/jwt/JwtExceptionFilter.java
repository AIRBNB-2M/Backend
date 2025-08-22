package project.airbnb.clone.config.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import project.airbnb.clone.common.advice.ErrorResponse;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JwtException e) {

            Throwable cause = e.getCause();
            log.debug("JWT 인증 예외: {} / {}", e.getMessage(), cause.getMessage());

            HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
            String message = "The token is invalid";

            if (cause instanceof ExpiredJwtException) {
                message = "The token has expired";
            }

            ErrorResponse<String> errorResponse = ErrorResponse.<String>builder()
                                                               .status(httpStatus.value())
                                                               .error(httpStatus.getReasonPhrase())
                                                               .message(message)
                                                               .path(request.getRequestURI())
                                                               .build();
            response.setStatus(httpStatus.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), errorResponse);
        }
    }
}
