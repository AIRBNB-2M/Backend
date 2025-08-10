package project.airbnb.clone.config.advice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import project.airbnb.clone.common.exceptions.EmailAlreadyExistsException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse<String>> handleEmailExistsException(EmailAlreadyExistsException e, HttpServletRequest request) {
        HttpStatus conflict = HttpStatus.CONFLICT;

        ErrorResponse<String> errorResponse = ErrorResponse.<String>builder()
                                                           .status(conflict.value())
                                                           .error(conflict.getReasonPhrase())
                                                           .message(e.getMessage())
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e, HttpServletRequest request) {
        HttpStatus serverError = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse<String> errorResponse = ErrorResponse.<String>builder()
                                                           .status(serverError.value())
                                                           .error(serverError.getReasonPhrase())
                                                           .message(e.getMessage())
                                                           .path(request.getRequestURI())
                                                           .build();

        return new ResponseEntity<>(errorResponse, serverError);
    }
}
