package project.airbnb.clone.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import project.airbnb.clone.entity.accommodation.Accommodation;
import project.airbnb.clone.entity.accommodation.AccommodationPrice;

import java.util.List;

public interface AccommodationPriceRepository extends JpaRepository<AccommodationPrice, Long> {
    @Modifying(clearAutomatically = true)
    void deleteByAccommodationIn(List<Accommodation> accommodations);
}