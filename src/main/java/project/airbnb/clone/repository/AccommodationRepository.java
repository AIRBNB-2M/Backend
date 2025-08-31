package project.airbnb.clone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.airbnb.clone.entity.Accommodation;

import java.util.List;
import java.util.Optional;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

	Optional<Accommodation> findByContentId(String tourApiId);

    List<Accommodation> findByContentIdIn(List<String> contentIds);
}
