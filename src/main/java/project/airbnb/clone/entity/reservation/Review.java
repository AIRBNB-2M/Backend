package project.airbnb.clone.entity.reservation;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.airbnb.clone.entity.BaseEntity;
import project.airbnb.clone.entity.member.Member;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reviews")
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id", nullable = false)
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "rating", nullable = false)
    private Double rating;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static Review create(double rating, String content, Reservation reservation, Member member) {
        return new Review(content, rating, reservation, member);
    }

    private Review(String content, Double rating, Reservation reservation, Member member) {
        this.content = content;
        this.rating = rating;
        this.reservation = reservation;
        this.member = member;
    }

    public void update(Double rating, String content) {
        this.rating = rating;
        this.content = content;
    }
}