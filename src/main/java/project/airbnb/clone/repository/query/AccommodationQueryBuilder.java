package project.airbnb.clone.repository.query;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import project.airbnb.clone.consts.DayType;
import project.airbnb.clone.consts.Season;
import project.airbnb.clone.repository.dto.DetailAccommodationQueryDto;
import project.airbnb.clone.repository.dto.FilteredAccListQueryDto;
import project.airbnb.clone.repository.dto.MainAccListQueryDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.querydsl.core.types.Projections.constructor;
import static project.airbnb.clone.entity.QAccommodation.accommodation;
import static project.airbnb.clone.entity.QAccommodationImage.accommodationImage;
import static project.airbnb.clone.entity.QAccommodationPrice.accommodationPrice;
import static project.airbnb.clone.entity.QAreaCode.areaCode;
import static project.airbnb.clone.entity.QReservation.reservation;
import static project.airbnb.clone.entity.QReview.review;
import static project.airbnb.clone.entity.QSigunguCode.sigunguCode;
import static project.airbnb.clone.entity.QWishlist.wishlist;
import static project.airbnb.clone.entity.QWishlistAccommodation.wishlistAccommodation;

public record AccommodationQueryBuilder(JPAQueryFactory queryFactory, DayType dayType, Season season, Long memberId) {

    // =====================================================
    // 공통 메서드
    // =====================================================
    private boolean hasMember() {
        return memberId != null;
    }

    private JPAQuery<?> baseQuery() {
        return queryFactory
                .from(accommodation)
                .join(accommodationPrice).on(
                        accommodationPrice.accommodation.eq(accommodation)
                                                        .and(accommodationPrice.season.eq(season))
                                                        .and(accommodationPrice.dayType.eq(dayType))
                );
    }

    private JPAQuery<?> withWishlistJoin(JPAQuery<?> query) {
        if (!hasMember()) {
            return query;
        }

        return query.leftJoin(wishlistAccommodation).on(wishlistAccommodation.accommodation.eq(accommodation))
                    .leftJoin(wishlistAccommodation.wishlist, wishlist).on(wishlist.member.id.eq(memberId));
    }

    // =====================================================
    // 메인 페이지용 쿼리
    // =====================================================
    public List<MainAccListQueryDto> fetchMainAccList() {
        JPAQuery<?> query = withWishlistJoin(buildAreaAccommodationsBaseQuery());

        return query.select(buildMainAccListProjection())
                    .groupBy(mainGroupBy())
                    .orderBy(reservation.count().desc())
                    .fetch();
    }

    /**
     * 메인 페이지용 베이스쿼리
     */
    private JPAQuery<?> buildAreaAccommodationsBaseQuery() {
        return baseQuery()
                .join(accommodationImage).on(accommodationImage.accommodation.eq(accommodation)
                                                                             .and(accommodationImage.thumbnail.isTrue()))
                .join(accommodation.sigunguCode, sigunguCode)
                .join(sigunguCode.areaCode, areaCode)
                .leftJoin(reservation).on(reservation.accommodation.eq(accommodation))
                .leftJoin(review).on(review.reservation.eq(reservation));
    }

    /**
     * 메인 페이지용 select
     */
    private Expression<MainAccListQueryDto> buildMainAccListProjection() {
        if (!hasMember()) {
            return constructor(MainAccListQueryDto.class,
                    accommodation.id,
                    accommodation.title,
                    accommodationPrice.price.max(),
                    review.rating.avg().coalesce(0.0),
                    accommodationImage.imageUrl,
                    reservation.count().coalesce(0L),
                    areaCode.codeName,
                    areaCode.code
            );
        }

        return constructor(MainAccListQueryDto.class,
                accommodation.id,
                accommodation.title,
                accommodationPrice.price.max(),
                review.rating.avg().coalesce(0.0),
                accommodationImage.imageUrl,
                wishlist.isNotNull(),
                wishlist.id,
                wishlist.name,
                reservation.count().coalesce(0L),
                areaCode.codeName,
                areaCode.code
        );
    }

    /**
     * 메인 페이지용 groupBy
     */
    private Expression<?>[] mainGroupBy() {
        List<Expression<?>> fields = new ArrayList<>(List.of(
                accommodation.id,
                accommodation.title,
                accommodationImage.imageUrl,
                areaCode.codeName,
                areaCode.code
        ));

        if (hasMember()) {
            fields.add(wishlist.id);
            fields.add(wishlist.name);
        }

        return fields.toArray(new Expression[0]);
    }


    // =====================================================
    // 검색 페이지용 쿼리
    // =====================================================
    public List<FilteredAccListQueryDto> fetchFilteredAccList(Pageable pageable, BooleanExpression... params) {
        JPAQuery<?> query = withWishlistJoin(buildFilteredBaseQuery());

        return query.select(buildFilteredProjection())
                    .where(params)
                    .groupBy(filteredGroupBy())
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();
    }

    /**
     * 검색 페이지용 베이스쿼리
     */
    public JPAQuery<?> buildFilteredBaseQuery() {
        return baseQuery()
                .join(accommodationImage).on(accommodationImage.accommodation.eq(accommodation))
                .join(accommodation.sigunguCode, sigunguCode)
                .join(sigunguCode.areaCode, areaCode)
                .leftJoin(reservation).on(reservation.accommodation.eq(accommodation))
                .leftJoin(review).on(review.reservation.eq(reservation));
    }

    /**
     * 검색 페이지용 Select
     */
    public Expression<FilteredAccListQueryDto> buildFilteredProjection() {
        if (!hasMember()) {
            return constructor(FilteredAccListQueryDto.class,
                    accommodation.id,
                    accommodation.title,
                    accommodationPrice.price,
                    review.rating.avg().coalesce(0.0),
                    review.count().intValue().coalesce(0)
            );
        }

        return constructor(FilteredAccListQueryDto.class,
                accommodation.id,
                accommodation.title,
                accommodationPrice.price,
                review.rating.avg().coalesce(0.0),
                review.count().intValue().coalesce(0),
                wishlist.isNotNull(),
                wishlist.id,
                wishlist.name
        );
    }

    /**
     * 검색 페이지용 groupBy
     */
    private Expression<?>[] filteredGroupBy() {
        List<Expression<?>> fields = new ArrayList<>(List.of(
                accommodation.id,
                accommodation.title,
                accommodationPrice.price
        ));

        if (hasMember()) {
            fields.add(wishlist.id);
            fields.add(wishlist.name);
        }

        return fields.toArray(new Expression[0]);
    }

    // =====================================================
    // 상세 페이지용 쿼리
    // =====================================================
    public Optional<DetailAccommodationQueryDto> fetchDetailAcc(Long accId) {
        JPAQuery<?> query = withWishlistJoin(buildDetailAccommodationBaseQuery());

        return Optional.ofNullable(
                query.select(buildDetailProjection(accId))
                     .where(accommodation.id.eq(accId))
                     .fetchOne()
        );
    }

    /**
     * 상세 페이지용 베이스쿼리
     */
    public JPAQuery<?> buildDetailAccommodationBaseQuery() {
        return baseQuery();
    }

    /**
     * 상세 페이지용 Select절
     */
    public Expression<DetailAccommodationQueryDto> buildDetailProjection(Long accId) {
        JPQLQuery<Double> avgRateSubquery = JPAExpressions.select(review.rating.avg().coalesce(0.0))
                                                          .from(review)
                                                          .join(review.reservation, reservation)
                                                          .where(reservation.accommodation.id.eq(accId));
        if (!hasMember()) {
            return constructor(DetailAccommodationQueryDto.class,
                    accommodation.id,
                    accommodation.title,
                    accommodation.maxPeople,
                    accommodation.address,
                    accommodation.mapX,
                    accommodation.mapY,
                    accommodation.checkIn,
                    accommodation.checkOut,
                    accommodation.description,
                    accommodation.number,
                    accommodation.refundRegulation,
                    accommodationPrice.price,
                    avgRateSubquery
            );
        }

        return constructor(DetailAccommodationQueryDto.class,
                accommodation.id,
                accommodation.title,
                accommodation.maxPeople,
                accommodation.address,
                accommodation.mapX,
                accommodation.mapY,
                accommodation.checkIn,
                accommodation.checkOut,
                accommodation.description,
                accommodation.number,
                accommodation.refundRegulation,
                accommodationPrice.price,
                wishlist.isNotNull(),
                wishlist.id,
                wishlist.name,
                avgRateSubquery
        );
    }
}
