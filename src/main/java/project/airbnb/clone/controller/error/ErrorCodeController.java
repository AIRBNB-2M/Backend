package project.airbnb.clone.controller.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.airbnb.clone.common.advice.ErrorResponse;
import project.airbnb.clone.common.exceptions.ErrorCode;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/errors")
public class ErrorCodeController {

    @GetMapping
    public ResponseEntity<List<ErrorResponse>> getErrors() {
        List<ErrorResponse> errors = Arrays.stream(ErrorCode.values())
                                           .map(ErrorResponse::from)
                                           .toList();
        return ResponseEntity.ok(errors);
    }
}
