package project.airbnb.clone.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.StringUtils;
import project.airbnb.clone.consts.payment.PaymentMethod;
import project.airbnb.clone.consts.payment.PaymentStatus;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "payments")
public class Payment extends BaseEntity {

    @Id
    private String paymentKey;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private int totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private LocalDateTime approvedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    private static final ZoneId ZONE_ASIA_SEOUL = ZoneId.of("Asia/Seoul");

    public static Payment of(JsonNode response, Reservation reservation) {
        String orderId = response.get("orderId").asText();
        String paymentKey = response.get("paymentKey").asText();

        PaymentStatus paymentStatus = PaymentStatus.of(response.get("status").asText());
        PaymentMethod paymentMethod = PaymentMethod.of(response.get("method").asText());

        LocalDateTime requestedAt = parseToLocalDateTime(response.get("requestedAt").asText());
        LocalDateTime approvedAt = parseToLocalDateTime(response.get("approvedAt").asText(null));

        int totalAmount = response.get("totalAmount").asInt();

        return new Payment(paymentKey, orderId, totalAmount, paymentStatus, requestedAt, paymentMethod, approvedAt, reservation);
    }

    private static LocalDateTime parseToLocalDateTime(String timestamp) {
        if (!StringUtils.hasText(timestamp)) {
            return null;
        }

        return OffsetDateTime.parse(timestamp).atZoneSameInstant(ZONE_ASIA_SEOUL).toLocalDateTime();
    }
}