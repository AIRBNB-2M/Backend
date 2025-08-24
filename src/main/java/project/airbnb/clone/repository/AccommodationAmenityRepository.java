package project.airbnb.clone.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.entity.AccommodationAmenity;
import project.airbnb.clone.entity.Amenity;

public interface AccommodationAmenityRepository extends JpaRepository<AccommodationAmenity, Long> {

    List<AccommodationAmenity> findByAccommodation(Accommodation accommodation);
}
