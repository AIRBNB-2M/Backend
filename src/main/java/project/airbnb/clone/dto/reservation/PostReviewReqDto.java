package project.airbnb.clone.dto.reservation;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PostReviewReqDto(@NotNull @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal rating,
                               @NotBlank @Size(max = 100) String content) {
}
