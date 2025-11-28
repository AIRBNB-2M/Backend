package project.airbnb.clone.common.advice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import project.airbnb.clone.common.exceptions.ErrorCode;

import java.util.List;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private String errorCode;
    private List<FieldErrorResponse> errors;

    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getHttpStatus().value(), errorCode.getMessage(), errorCode.getCode(), null);
    }

    public static ErrorResponse of(ErrorCode errorCode, String detailMessage) {
        return new ErrorResponse(errorCode.getHttpStatus().value(), detailMessage, errorCode.getCode(), null);
    }

    public static ErrorResponse withFieldError(List<FieldErrorResponse> errors) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT;
        return new ErrorResponse(errorCode.getHttpStatus().value(), errorCode.getMessage(), errorCode.getCode(), errors);
    }

    public record FieldErrorResponse(String field, Object rejectedValue, String message) {}
}
