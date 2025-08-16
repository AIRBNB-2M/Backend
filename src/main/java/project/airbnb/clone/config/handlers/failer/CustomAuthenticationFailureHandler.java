package project.airbnb.clone.config.handlers.failer;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import project.airbnb.clone.common.exceptions.EmailAlreadyExistsException;
import project.airbnb.clone.config.advice.ErrorResponse;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.debug("인증 실패 : {}", exception.getMessage());

        ErrorResponse<String> errorResponse;

        HttpStatus httpStatus;
        String message;

        Throwable cause = exception.getCause();

        if (cause instanceof EmailAlreadyExistsException) {
            httpStatus = HttpStatus.CONFLICT;
            message = "가입된 정보가 있는 이메일입니다.";
        } else {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "서버 내부 오류 발생";
        }

        errorResponse = ErrorResponse.<String>builder()
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
