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
    @UniqueConstraint(name = "uk_view_histories_guest_accommodation", columnNames = {"guest_id", "accommodation_id"})
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
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    public static ViewHistory ofNow(Accommodation accommodation, Guest guest) {
        return new ViewHistory(accommodation, guest, LocalDateTime.now());
    }

    public static ViewHistory create(Guest guest, Accommodation accommodation, LocalDateTime viewedAt) {
        return new ViewHistory(accommodation, guest, viewedAt);
    }

    private ViewHistory(Accommodation accommodation, Guest guest, LocalDateTime viewedAt) {
        this.accommodation = accommodation;
        this.guest = guest;
        this.viewedAt = viewedAt;
    }
}