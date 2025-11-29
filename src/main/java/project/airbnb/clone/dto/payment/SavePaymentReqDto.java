package project.airbnb.clone.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SavePaymentReqDto(
        @NotBlank
        String orderId,

        @NotNull @Positive
        Integer amount) {
}
