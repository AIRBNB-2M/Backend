package project.airbnb.clone.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.entity.Wishlist;
import project.airbnb.clone.entity.WishlistAccommodation;

public interface WishlistAccommodationRepository extends JpaRepository<WishlistAccommodation, Long> {

    @Modifying(clearAutomatically = true)
    void deleteByWishlistAndAccommodation(Wishlist wishlist, Accommodation accommodation);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM WishlistAccommodation wa WHERE wa.wishlist = :wishlist")
    void deleteByWishlist(@Param("wishlist") Wishlist wishlist);
}