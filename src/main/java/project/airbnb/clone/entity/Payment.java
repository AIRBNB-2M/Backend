package project.airbnb.clone.entity;

import jakarta.persistence.*;
import lombok.*;
import project.airbnb.clone.consts.PayMethod;
import project.airbnb.clone.consts.PayStatus;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_pay_imp_uid", columnList = "imp_uid"),
        @Index(name = "idx_pay_reservation_id", columnList = "reservation_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_pay_merchant_uid", columnNames = "merchant_uid")
    }
)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false)
    private Long id;
    
    @Column(name = "merchant_uid", nullable = false, length = 60)
    private String merchantUid; // 주문번호
    
    @Column(name = "imp_uid", length = 60)
    private String impUid; // 포트원 결제 고유ID

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = true, length = 20)
    private PayMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PayStatus status;
    
    @Column(name = "amount", nullable = false)
    private Integer amount; // 결제 금액
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt; // 결제 완료 시각

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;
    
    public void setImpUid(String impUid) { this.impUid = impUid; }
    public void setMethod(PayMethod method) { this.method = method; }
    public void setStatus(PayStatus status) { this.status = status; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public void setAmount(Integer amount) { this.amount = amount; }
}