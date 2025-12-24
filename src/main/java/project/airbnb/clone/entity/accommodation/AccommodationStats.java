package project.airbnb.clone.entity.accommodation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "accommodation_stats")
public class AccommodationStats {

    @Id
    @Column(name = "stat_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "accommodation_id", nullable = false)
    private Long accommodationId;

    @Column(name = "area_code", nullable = false)
    private String areaCode;

    @Column(name = "area_name", nullable = false)
    private String areaName;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "reservation_count")
    private Integer reservationCount;

    @Column(name = "thumbnail_url", nullable = false)
    private String thumbnailUrl;
}
