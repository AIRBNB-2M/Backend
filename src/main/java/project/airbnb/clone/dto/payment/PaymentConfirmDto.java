package project.airbnb.clone.dto.payment;

public record PaymentConfirmDto(
        String paymentKey,
        String orderId,
        Integer amount) {
}
