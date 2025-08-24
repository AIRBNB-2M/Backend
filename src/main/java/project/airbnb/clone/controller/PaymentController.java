package project.airbnb.clone.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import project.airbnb.clone.dto.PaymentDtos.CompleteRequest;
import project.airbnb.clone.dto.PaymentDtos.CompleteResponse;
import project.airbnb.clone.dto.PaymentDtos.PrepareRequest;
import project.airbnb.clone.dto.PaymentDtos.PrepareResponse;
import project.airbnb.clone.service.PaymentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

	private final PaymentService paymentService;

    @PostMapping("/prepare")
    public ResponseEntity<PrepareResponse> prepare(@RequestBody PrepareRequest req) {
        return ResponseEntity.ok(paymentService.prepare(req));
    }

    @PostMapping("/complete")
    public ResponseEntity<CompleteResponse> complete(@RequestBody CompleteRequest req) {
        return ResponseEntity.ok(paymentService.complete(req));
    }
}
