package project.airbnb.clone.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import project.airbnb.clone.entity.WishlistAccommodation;

public interface WishlistAccommodationRepository extends JpaRepository<WishlistAccommodation, Long> {
}