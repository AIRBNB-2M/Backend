package project.airbnb.clone.config.security.handlers.failer;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import project.airbnb.clone.config.advice.ErrorResponse;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.debug("인증 예외: {}", authException.getMessage());

        Throwable cause = authException.getCause();
        HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;

        String message = "The token is invalid";

        if (cause instanceof CredentialsExpiredException) {
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
