package project.airbnb.clone.config.security.handlers.failer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import project.airbnb.clone.common.advice.ErrorResponse;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final JsonMapper jsonMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.debug("REST 인증 오류: {}", exception.getMessage());

        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        Throwable cause = exception.getCause();

        if (cause instanceof BadCredentialsException) {
            httpStatus = HttpStatus.BAD_REQUEST;
        }

        ErrorResponse<String> errorResponse = ErrorResponse.<String>builder()
                                                           .status(httpStatus.value())
                                                           .error(httpStatus.getReasonPhrase())
                                                           .message(null)
                                                           .path(request.getRequestURI())
                                                           .build();

        response.setStatus(httpStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(response.getWriter(), errorResponse);
    }
}
