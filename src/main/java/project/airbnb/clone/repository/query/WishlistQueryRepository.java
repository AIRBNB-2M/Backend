package project.airbnb.clone.repository.query;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.stereotype.Repository;
import project.airbnb.clone.dto.wishlist.WishlistsResDto;
import project.airbnb.clone.entity.QWishlistAccommodation;
import project.airbnb.clone.entity.Wishlist;
import project.airbnb.clone.repository.dto.AccAllImagesQueryDto;
import project.airbnb.clone.repository.dto.WishlistDetailQueryDto;
import project.airbnb.clone.repository.query.support.CustomQuerydslRepositorySupport;

import java.util.List;

import static com.querydsl.core.types.Projections.constructor;
import static project.airbnb.clone.entity.QAccommodation.accommodation;
import static project.airbnb.clone.entity.QAccommodationImage.accommodationImage;
import static project.airbnb.clone.entity.QGuest.guest;
import static project.airbnb.clone.entity.QReservation.reservation;
import static project.airbnb.clone.entity.QReview.review;
import static project.airbnb.clone.entity.QWishlist.wishlist;
import static project.airbnb.clone.entity.QWishlistAccommodation.wishlistAccommodation;

@Repository
public class WishlistQueryRepository extends CustomQuerydslRepositorySupport {

    public WishlistQueryRepository() {
        super(Wishlist.class);
    }

    public boolean existsWishlistAccommodation(Long wishlistId, Long accommodationId, Long guestId) {
        return getQueryFactory()
                .selectOne()
                .from(wishlistAccommodation)
                .join(wishlistAccommodation.wishlist, wishlist)
                .where(wishlistAccommodation.accommodation.id.eq(accommodationId),
                        wishlistAccommodation.wishlist.id.eq(wishlistId),
                        wishlist.guest.id.eq(guestId)
                )
                .fetchFirst() != null;
    }

    public List<WishlistDetailQueryDto> findWishlistDetails(Long wishlistId, Long guestId) {
        return select(constructor(WishlistDetailQueryDto.class,
                accommodation.id,
                wishlist.name,
                accommodation.title,
                accommodation.description,
                accommodation.mapX,
                accommodation.mapY,
                review.rating.avg().coalesce(0.0),
                wishlistAccommodation.memo
        ))
                .from(wishlistAccommodation)
                .join(wishlistAccommodation.wishlist, wishlist)
                .join(wishlistAccommodation.accommodation, accommodation)
                .leftJoin(reservation).on(reservation.accommodation.eq(accommodation))
                .leftJoin(review).on(review.reservation.eq(reservation))
                .where(wishlist.id.eq(wishlistId),
                        wishlist.guest.id.eq(guestId)
                )
                .groupBy(accommodation.id, wishlist.name, accommodation.title, accommodation.description, accommodation.mapX, accommodation.mapY, wishlistAccommodation.memo)
                .fetch();
    }

    public List<AccAllImagesQueryDto> findAllImages(List<Long> accIds) {
        return select(constructor(AccAllImagesQueryDto.class,
                accommodationImage.accommodation.id,
                accommodationImage.imageUrl))
                .from(accommodationImage)
                .where(accommodationImage.accommodation.id.in(accIds))
                .fetch();
    }

    public List<WishlistsResDto> getAllWishlists(Long guestId) {
        QWishlistAccommodation waSub = new QWishlistAccommodation("waSub");
        JPQLQuery<Long> recentAccIdSubquery = JPAExpressions.select(waSub.accommodation.id)
                                                            .from(waSub)
                                                            .where(waSub.wishlist.eq(wishlist)
                                                                                 .and(waSub.id.eq(
                                                                                         JPAExpressions.select(waSub.id.max())
                                                                                                       .from(waSub)
                                                                                                       .where(waSub.wishlist.eq(wishlist))
                                                                                 )));

        return select(constructor(WishlistsResDto.class,
                wishlist.id,
                wishlist.name,
                accommodationImage.imageUrl,
                wishlistAccommodation.accommodation.count().intValue().coalesce(0)))
                .from(wishlist)
                .join(wishlist.guest, guest)
                .leftJoin(wishlistAccommodation).on(wishlistAccommodation.wishlist.eq(wishlist))
                .leftJoin(accommodation).on(accommodation.id.eq(recentAccIdSubquery))
                .leftJoin(accommodationImage)
                .on(accommodationImage.accommodation.eq(accommodation).and(accommodationImage.thumbnail.isTrue()))
                .where(guest.id.eq(guestId))
                .groupBy(wishlist.id, wishlist.name, accommodationImage.imageUrl)
                .fetch();
    }
}
