package project.airbnb.clone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.airbnb.clone.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}