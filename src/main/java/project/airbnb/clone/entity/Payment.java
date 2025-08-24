package project.airbnb.clone.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.airbnb.clone.consts.PayMethod;
import project.airbnb.clone.consts.PayStatus;

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