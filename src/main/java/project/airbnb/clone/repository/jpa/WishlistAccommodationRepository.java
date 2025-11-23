package project.airbnb.clone.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.entity.Wishlist;
import project.airbnb.clone.entity.WishlistAccommodation;

import java.util.Optional;

public interface WishlistAccommodationRepository extends JpaRepository<WishlistAccommodation, Long> {

    @Modifying(clearAutomatically = true)
    void deleteByWishlistAndAccommodation(Wishlist wishlist, Accommodation accommodation);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM WishlistAccommodation wa WHERE wa.wishlist = :wishlist")
    void deleteByWishlist(@Param("wishlist") Wishlist wishlist);

    @Query("""
                SELECT wa
                FROM WishlistAccommodation wa
                JOIN wa.wishlist w
                WHERE wa.accommodation.id = :accommodationId
                AND w.id = :wishlistId
                AND w.member.id = :memberId
            """)
    Optional<WishlistAccommodation> findByAllIds(@Param("wishlistId") Long wishlistId,
                                                 @Param("accommodationId") Long accommodationId,
                                                 @Param("memberId") Long memberId);
}