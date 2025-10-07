package project.airbnb.clone.common.advice;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
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
import project.airbnb.clone.common.exceptions.chat.ChatException;
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
        ErrorResponse<String> errorResponse = createErrorResponse(conflict, "Email already exists", request);

        return new ResponseEntity<>(errorResponse, conflict);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        log.debug("ExceptionControllerAdvice.handleMethodArgumentNotValid");

        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<String> errorList = fieldErrors.stream()
                                            .map(error -> error.getField() + " : " + error.getDefaultMessage())
                                            .toList();

        ServletWebRequest servletWebRequest = (ServletWebRequest) request;
        ErrorResponse<List<String>> errorResponse = createErrorResponse(status, errorList, null, servletWebRequest.getRequest());

        return new ResponseEntity<>(errorResponse, headers, status);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<?> handleJwtException(JwtException e, HttpServletRequest request) {
        log.debug("ExceptionControllerAdvice.handleJwtException: {}", e.getMessage());

        HttpStatus unauthorized = HttpStatus.UNAUTHORIZED;

        String message = "Token is invalid";
        if (e instanceof ExpiredJwtException) {
            message = "Token has expired";
        }

        String key = (String) request.getAttribute("key");
        redisRepository.deleteValue(key);

        ErrorResponse<String> errorResponse = createErrorResponse(unauthorized, message, request);

        return new ResponseEntity<>(errorResponse, unauthorized);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.debug("ExceptionControllerAdvice.AuthenticationException: {}", e.getMessage());

        HttpStatus unauthorized = HttpStatus.UNAUTHORIZED;
        ErrorResponse<String> errorResponse = createErrorResponse(unauthorized, "unauthorized", request);

        return new ResponseEntity<>(errorResponse, unauthorized);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.debug("ExceptionControllerAdvice.AccessDeniedException: {}", e.getMessage());

        HttpStatus forbidden = HttpStatus.FORBIDDEN;
        ErrorResponse<String> errorResponse = createErrorResponse(forbidden, "Access Denied", request);

        return new ResponseEntity<>(errorResponse, forbidden);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFoundException(EntityNotFoundException e, HttpServletRequest request) {
        log.debug("ExceptionControllerAdvice.EntityNotFoundException: {}", e.getMessage());

        HttpStatus notFound = HttpStatus.NOT_FOUND;
        ErrorResponse<String> errorResponse = createErrorResponse(notFound, "Data not found in the database", request);

        return new ResponseEntity<>(errorResponse, notFound);
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<ErrorResponse<String>> handleMailSendException(MailException e, HttpServletRequest request) {
        log.debug("ExceptionControllerAdvice.handleMailSendException: {}", e.getMessage());

        HttpStatus serverError = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse<String> errorResponse = createErrorResponse(serverError, "Email verification send failed", request);

        return new ResponseEntity<>(errorResponse, serverError);
    }

    @ExceptionHandler(ChatException.class)
    public ResponseEntity<?> handleChatException(ChatException e, HttpServletRequest request) {
        log.debug("ExceptionControllerAdvice.ChatException: {}", e.getMessage());

        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        ErrorResponse<String> errorResponse = createErrorResponse(badRequest, "Chat is in an invalid state", request);

        return new ResponseEntity<>(errorResponse, badRequest);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e, HttpServletRequest request) {
        log.debug("ExceptionControllerAdvice.handleException: {}", e.getMessage());

        HttpStatus serverError = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse<String> errorResponse = createErrorResponse(serverError, "Server error", request);

        return new ResponseEntity<>(errorResponse, serverError);
    }

    private <T> ErrorResponse<T> createErrorResponse(HttpStatusCode httpStatus, T error, String message, HttpServletRequest request) {
        return ErrorResponse.<T>builder()
                            .status(httpStatus.value())
                            .error(error)
                            .message(message)
                            .path(request.getMethod() + ": " + request.getRequestURI())
                            .build();
    }

    private ErrorResponse<String> createErrorResponse(HttpStatus httpStatus, String message, HttpServletRequest request) {
        return createErrorResponse(httpStatus, httpStatus.getReasonPhrase(), message, request);
    }
}
