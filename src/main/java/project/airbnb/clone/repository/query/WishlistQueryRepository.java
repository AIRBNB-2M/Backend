package project.airbnb.clone.repository.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.airbnb.clone.entity.QAccommodation;
import project.airbnb.clone.entity.QAccommodationImage;
import project.airbnb.clone.entity.QReservation;
import project.airbnb.clone.entity.QReview;
import project.airbnb.clone.entity.QWishlist;
import project.airbnb.clone.entity.QWishlistAccommodation;
import project.airbnb.clone.repository.dto.AccAllImagesQueryDto;
import project.airbnb.clone.repository.dto.WishlistDetailQueryDto;

import java.util.List;

import static com.querydsl.core.types.Projections.constructor;
import static project.airbnb.clone.entity.QAccommodation.accommodation;
import static project.airbnb.clone.entity.QAccommodationImage.accommodationImage;
import static project.airbnb.clone.entity.QReservation.reservation;
import static project.airbnb.clone.entity.QReview.review;
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

    public List<WishlistDetailQueryDto> findWishlistDetails(Long wishlistId, Long guestId) {
        QWishlistAccommodation wa = wishlistAccommodation;
        QAccommodation acc = accommodation;
        QReservation rs = reservation;
        QReview r = review;
        QWishlist w = wishlist;

        return queryFactory
                .select(constructor(WishlistDetailQueryDto.class,
                        acc.id,
                        acc.title,
                        acc.description,
                        acc.mapX,
                        acc.mapY,
                        r.rating.avg().coalesce(0.0),
                        wa.memo
                ))
                .from(wa)
                .join(wa.wishlist, w)
                .join(wa.accommodation, acc)
                .leftJoin(rs).on(rs.accommodation.eq(acc))
                .leftJoin(r).on(r.reservation.eq(rs))
                .where(w.id.eq(wishlistId),
                        w.guest.id.eq(guestId)
                )
                .groupBy(acc.id, acc.title, acc.description, acc.mapX, acc.mapY, wa.memo)
                .fetch();
    }

    public List<AccAllImagesQueryDto> findAllImages(List<Long> accIds) {
        QAccommodationImage ai = accommodationImage;
        return queryFactory.select(constructor(AccAllImagesQueryDto.class,
                                   ai.accommodation.id,
                                   ai.imageUrl
                           ))
                           .from(ai)
                           .where(ai.accommodation.id.in(accIds))
                           .fetch();
    }
}
