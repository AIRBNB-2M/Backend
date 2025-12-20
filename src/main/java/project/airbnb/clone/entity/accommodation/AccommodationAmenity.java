package project.airbnb.clone.entity.accommodation;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.airbnb.clone.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	    name = "accommodation_amenities",
	    uniqueConstraints = {
	        @UniqueConstraint(
	            name = "uk_acc_amenity",
	            columnNames = {"accommodation_id", "amenity_id"}
	        )
	    }
	)
public class AccommodationAmenity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accommodation_amenities_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amenity_id", nullable = false)
    private Amenity amenity;

    public static AccommodationAmenity create(Accommodation accommodation, Amenity amenity) {
        return new AccommodationAmenity(accommodation, amenity);
    }

    private AccommodationAmenity(Accommodation accommodation, Amenity amenity) {
        this.accommodation = accommodation;
        this.amenity = amenity;
    }
}