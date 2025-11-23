package project.airbnb.clone.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "view_histories", uniqueConstraints =
    @UniqueConstraint(name = "uk_view_histories_member_accommodation", columnNames = {"member_id", "accommodation_id"})
)
public class ViewHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "view_history_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    public static ViewHistory ofNow(Accommodation accommodation, Member member) {
        return new ViewHistory(accommodation, member, LocalDateTime.now());
    }

    public static ViewHistory create(Member member, Accommodation accommodation, LocalDateTime viewedAt) {
        return new ViewHistory(accommodation, member, viewedAt);
    }

    private ViewHistory(Accommodation accommodation, Member member, LocalDateTime viewedAt) {
        this.accommodation = accommodation;
        this.member = member;
        this.viewedAt = viewedAt;
    }
}