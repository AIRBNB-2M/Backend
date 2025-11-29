package project.airbnb.clone.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentConfirmReqDto(
        @NotBlank
        String paymentKey,

        @NotBlank
        String orderId,

        @NotNull @Positive
        Integer amount,

        @NotNull
        Long reservationId)
{
    public PaymentConfirmDto convert() {
        return new PaymentConfirmDto(this.paymentKey, this.orderId, this.amount);
    }
}
