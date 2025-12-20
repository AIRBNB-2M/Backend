package project.airbnb.clone.entity.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.airbnb.clone.consts.Reason;
import project.airbnb.clone.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "withdrawal_reason")
public class WithdrawalReason extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "withdrawal_reason_Id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private Reason reason;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}