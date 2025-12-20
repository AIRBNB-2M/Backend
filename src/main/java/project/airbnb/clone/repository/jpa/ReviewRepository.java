package project.airbnb.clone.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import project.airbnb.clone.entity.reservation.Review;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByIdAndMemberId(Long id, Long memberId);
}