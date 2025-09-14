package project.airbnb.clone.repository.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.airbnb.clone.entity.QWishlist;
import project.airbnb.clone.entity.QWishlistAccommodation;

import static project.airbnb.clone.entity.QWishlist.wishlist;
import static project.airbnb.clone.entity.QWishlistAccommodation.wishlistAccommodation;

@Repository
@RequiredArgsConstructor
public class WishlistQueryRepository {

    private final JPAQueryFactory queryFactory;

    public boolean existsWishlistAccommodation(Long wishlistId, Long accommodationId, Long guestId) {
        QWishlistAccommodation wa = wishlistAccommodation;
        QWishlist w = wishlist;
        return queryFactory
                .selectOne()
                .from(wa)
                .join(wa.wishlist, w)
                .where(wa.accommodation.id.eq(accommodationId),
                        wa.wishlist.id.eq(wishlistId),
                        w.guest.id.eq(guestId)
                )
                .fetchFirst() != null;
    }
}
