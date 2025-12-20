package project.airbnb.clone.entity.accommodation;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.airbnb.clone.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "accommodation_images")
public class AccommodationImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accommodation_image_id", nullable = false)
    private Long id;

    @Column(name = "image_url", nullable = false, length = 700)
    private String imageUrl;

    @Column(name = "thumbnail", nullable = false)
    private boolean thumbnail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;

    public static AccommodationImage thumbnailOf(Accommodation accommodation, String thumbnailUrl) {
        return new AccommodationImage(thumbnailUrl, true, accommodation);
    }

    public static AccommodationImage normalOf(Accommodation accommodation, String thumbnailUrl) {
        return new AccommodationImage(thumbnailUrl, false, accommodation);
    }

    private AccommodationImage(String imageUrl, boolean thumbnail, Accommodation accommodation) {
        this.imageUrl = imageUrl;
        this.thumbnail = thumbnail;
        this.accommodation = accommodation;
    }
}