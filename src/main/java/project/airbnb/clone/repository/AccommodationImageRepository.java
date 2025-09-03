package project.airbnb.clone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.entity.AccommodationImage;

import java.util.List;

public interface AccommodationImageRepository extends JpaRepository<AccommodationImage, Long> {

    List<AccommodationImage> findByAccommodation(Accommodation accommodation);

    @Modifying(clearAutomatically = true)
    void deleteByAccommodation(Accommodation accommodation);
}
