package project.airbnb.clone.controller.payment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.airbnb.clone.common.annotations.CurrentMemberId;
import project.airbnb.clone.dto.payment.PaymentConfirmReqDto;
import project.airbnb.clone.dto.payment.PaymentResDto;
import project.airbnb.clone.dto.payment.SavePaymentReqDto;
import project.airbnb.clone.service.payment.PaymentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/save")
    public ResponseEntity<?> savePayment(@Valid @RequestBody SavePaymentReqDto savePaymentReqDto) {
        paymentService.savePayment(savePaymentReqDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResDto> confirmPayment(@CurrentMemberId Long memberId,
                                                        @Valid @RequestBody PaymentConfirmReqDto paymentConfirmReqDto) {
        PaymentResDto response = paymentService.confirmPayment(paymentConfirmReqDto, memberId);
        return ResponseEntity.ok(response);
    }
}
