package project.airbnb.clone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.entity.AccommodationAmenity;

import java.util.List;

public interface AccommodationAmenityRepository extends JpaRepository<AccommodationAmenity, Long> {

    List<AccommodationAmenity> findByAccommodation(Accommodation accommodation);

    @Modifying(clearAutomatically = true)
    void deleteByAccommodation(Accommodation accommodation);
}
