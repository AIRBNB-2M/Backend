package project.airbnb.clone.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import project.airbnb.clone.entity.Wishlist;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
}