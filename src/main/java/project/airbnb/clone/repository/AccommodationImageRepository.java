package project.airbnb.clone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.entity.AccommodationImage;

public interface AccommodationImageRepository extends JpaRepository<AccommodationImage, Long> {

    List<AccommodationImage> findByAccommodation(Accommodation accommodation);
}
