package project.airbnb.clone.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import project.airbnb.clone.consts.PayMethod;
import project.airbnb.clone.consts.PayStatus;
import project.airbnb.clone.dto.PaymentDtos.CompleteRequest;
import project.airbnb.clone.dto.PaymentDtos.CompleteResponse;
import project.airbnb.clone.dto.PaymentDtos.PrepareRequest;
import project.airbnb.clone.dto.PaymentDtos.PrepareResponse;
import project.airbnb.clone.entity.Payment;
import project.airbnb.clone.entity.Reservation;
import project.airbnb.clone.infra.PortOneClient;
import project.airbnb.clone.repository.jpa.PaymentRepository;
import project.airbnb.clone.repository.jpa.ReservationRepository;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final PortOneClient portOneClient;

    @Transactional
    public PrepareResponse prepare(PrepareRequest req) {
        Reservation reservation = reservationRepository.findById(req.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("예약 미존재: " + req.getReservationId()));

        Payment payment = paymentRepository.findByMerchantUid(req.getMerchantUid())
                .orElse(Payment.builder()
                        .reservation(reservation)
                        .merchantUid(req.getMerchantUid())
                        .amount(req.getAmount())
                        .status(PayStatus.READY)
                        .build());

        payment.setAmount(req.getAmount());
        payment.setStatus(PayStatus.READY);
        paymentRepository.save(payment);

        portOneClient.prepare(req.getMerchantUid(), req.getAmount());

        return PrepareResponse.builder()
                .merchantUid(payment.getMerchantUid())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .build();
    }

    @Transactional
    public CompleteResponse complete(CompleteRequest req) {
        var detail = portOneClient.getPayment(req.getImpUid());

        Payment payment = paymentRepository.findByMerchantUid(req.getMerchantUid())
                .orElseThrow(() -> new IllegalArgumentException("주문 미존재: " + req.getMerchantUid()));

        // 검증
        if (!req.getMerchantUid().equals(detail.getMerchant_uid()))
            throw new IllegalStateException("merchantUid 불일치");
        if (!payment.getAmount().equals(detail.getAmount()))
            throw new IllegalStateException("금액 불일치");

        // 반영
        payment.setImpUid(detail.getImp_uid());
        if ("paid".equalsIgnoreCase(detail.getStatus())) {
            payment.setStatus(PayStatus.PAID);
            // pay_method 매핑
            if (detail.getPay_method() != null) {
                PayMethod m = switch (detail.getPay_method()) {
                    case "kakaopay" -> PayMethod.KAKAO;
                    case "naverpay" -> PayMethod.NAVER;
                    default -> PayMethod.CARD;
                };
                payment.setMethod(m);
            }
            if (detail.getPaid_at() != null) {
                payment.setPaidAt(LocalDateTime.ofInstant(Instant.ofEpochSecond(detail.getPaid_at()), ZoneId.systemDefault()));
            }
        } else {
            payment.setStatus(PayStatus.FAILED);
        }
        paymentRepository.save(payment);

        // TODO: 결제 성공 시 예약 상태 갱신(필요하다면)
        return CompleteResponse.builder()
                .merchantUid(payment.getMerchantUid())
                .impUid(payment.getImpUid())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .build();
    }
}
