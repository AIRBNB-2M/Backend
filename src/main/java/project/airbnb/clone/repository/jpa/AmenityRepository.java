package project.airbnb.clone.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import project.airbnb.clone.entity.accommodation.Amenity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {

	Optional<Amenity> findByName(String name);
	List<Amenity> findAllByNameIn(Collection<String> names);
}
