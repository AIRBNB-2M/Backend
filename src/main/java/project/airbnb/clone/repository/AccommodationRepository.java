package project.airbnb.clone.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import project.airbnb.clone.entity.Accommodation;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

	Optional<Accommodation> findByTourApiId(String tourApiId);
}
