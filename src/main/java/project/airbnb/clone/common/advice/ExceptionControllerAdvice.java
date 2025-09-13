package project.airbnb.clone.common.advice;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import project.airbnb.clone.common.exceptions.EmailAlreadyExistsException;
import project.airbnb.clone.repository.redis.RedisRepository;

import java.util.List;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    private final RedisRepository redisRepository;

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse<String>> handleEmailExistsException(EmailAlreadyExistsException e, HttpServletRequest request) {
        log.debug("ExceptionControllerAdvice.handleEmailExistsException: {}", e.getMessage());

        HttpStatus conflict = HttpStatus.CONFLICT;

        ErrorResponse<String> errorResponse = ErrorResponse.<String>builder()
                                                           .status(conflict.value())
                                                           .error(conflict.getReasonPhrase())
                                                           .message("Email already exists")
                                                           .path(request.getRequestURI())
                                                           .build();

        return new ResponseEntity<>(errorResponse, conflict);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request)
    {
        log.debug("ExceptionControllerAdvice.handleMethodArgumentNotValid");

        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<String> errorList = fieldErrors.stream()
                                            .map(error -> error.getField() + " : " + error.getDefaultMessage())
                                            .toList();

        ServletWebRequest servletWebRequest = (ServletWebRequest) request;
        String requestURI = servletWebRequest.getRequest().getRequestURI();

        ErrorResponse<List<String>> errorResponse = ErrorResponse.<List<String>>builder()
                                                                 .status(status.value())
                                                                 .error(errorList)
                                                                 .message(null)
                                                                 .path(requestURI)
                                                                 .build();

        return new ResponseEntity<>(errorResponse, headers, status);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<?> handleJwtException(JwtException e, HttpServletRequest request) {
        log.debug("ExceptionControllerAdvice.handleJwtException: {}", e.getMessage());

        HttpStatus serverError = HttpStatus.UNAUTHORIZED;

        String message = "Token is invalid";
        if (e instanceof ExpiredJwtException) {
            message = "Token has expired";
        }

        String key = (String) request.getAttribute("key");
        redisRepository.deleteValue(key);

        ErrorResponse<String> errorResponse = ErrorResponse.<String>builder()
                                                           .status(serverError.value())
                                                           .error(serverError.getReasonPhrase())
                                                           .message(message)
                                                           .path(request.getRequestURI())
                                                           .build();

        return new ResponseEntity<>(errorResponse, serverError);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.debug("ExceptionControllerAdvice.AuthenticationException: {}", e.getMessage());

        HttpStatus unauthorized = HttpStatus.UNAUTHORIZED;

        ErrorResponse<String> errorResponse = ErrorResponse.<String>builder()
                                                           .status(unauthorized.value())
                                                           .error(unauthorized.getReasonPhrase())
                                                           .message("unauthorized")
                                                           .path(request.getRequestURI())
                                                           .build();

        return new ResponseEntity<>(errorResponse, unauthorized);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.debug("ExceptionControllerAdvice.AccessDeniedException: {}", e.getMessage());

        HttpStatus forbidden = HttpStatus.FORBIDDEN;

        ErrorResponse<String> errorResponse = ErrorResponse.<String>builder()
                                                           .status(forbidden.value())
                                                           .error(forbidden.getReasonPhrase())
                                                           .message("Access Denied")
                                                           .path(request.getRequestURI())
                                                           .build();

        return new ResponseEntity<>(errorResponse, forbidden);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e, HttpServletRequest request) {
        log.debug("ExceptionControllerAdvice.handleException: {}", e.getMessage());

        HttpStatus serverError = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse<String> errorResponse = ErrorResponse.<String>builder()
                                                           .status(serverError.value())
                                                           .error(serverError.getReasonPhrase())
                                                           .message("Server error")
                                                           .path(request.getRequestURI())
                                                           .build();

        return new ResponseEntity<>(errorResponse, serverError);
    }
}
