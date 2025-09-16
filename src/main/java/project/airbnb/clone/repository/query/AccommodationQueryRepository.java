package project.airbnb.clone.repository.query;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import project.airbnb.clone.consts.DayType;
import project.airbnb.clone.consts.Season;
import project.airbnb.clone.dto.accommodation.AccSearchCondDto;
import project.airbnb.clone.dto.accommodation.DetailAccommodationResDto.DetailReviewDto;
import project.airbnb.clone.dto.accommodation.FilteredAccListResDto;
import project.airbnb.clone.entity.QAccommodation;
import project.airbnb.clone.entity.QAccommodationAmenity;
import project.airbnb.clone.entity.QAccommodationImage;
import project.airbnb.clone.entity.QAccommodationPrice;
import project.airbnb.clone.entity.QAmenity;
import project.airbnb.clone.entity.QAreaCode;
import project.airbnb.clone.entity.QGuest;
import project.airbnb.clone.entity.QReservation;
import project.airbnb.clone.entity.QReview;
import project.airbnb.clone.entity.QWishlistAccommodation;
import project.airbnb.clone.repository.dto.DetailAccommodationQueryDto;
import project.airbnb.clone.repository.dto.ImageDataQueryDto;
import project.airbnb.clone.repository.dto.MainAccListQueryDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.querydsl.core.types.Projections.constructor;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.StringUtils.hasText;
import static project.airbnb.clone.entity.QAccommodation.accommodation;
import static project.airbnb.clone.entity.QAccommodationAmenity.accommodationAmenity;
import static project.airbnb.clone.entity.QAccommodationImage.accommodationImage;
import static project.airbnb.clone.entity.QAccommodationPrice.accommodationPrice;
import static project.airbnb.clone.entity.QAmenity.amenity;
import static project.airbnb.clone.entity.QAreaCode.areaCode;
import static project.airbnb.clone.entity.QGuest.guest;
import static project.airbnb.clone.entity.QReservation.reservation;
import static project.airbnb.clone.entity.QReview.review;
import static project.airbnb.clone.entity.QSigunguCode.sigunguCode;
import static project.airbnb.clone.entity.QWishlistAccommodation.wishlistAccommodation;

@Repository
@RequiredArgsConstructor
public class AccommodationQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<MainAccListQueryDto> getAreaAccommodations(Season season, DayType dayType, Long guestId) {
        QAccommodation acc = accommodation;
        QAccommodationImage ai = accommodationImage;
        QAccommodationPrice ap = accommodationPrice;
        QReservation rs = reservation;
        QReview rv = review;
        QAreaCode ac = areaCode;
        QWishlistAccommodation wa = wishlistAccommodation;

        return queryFactory
                .select(constructor(MainAccListQueryDto.class,
                        acc.id,
                        acc.title,
                        ap.price,
                        rv.rating.avg().coalesce(0.0),
                        ai.imageUrl,
                        isInWishlist(wa),
                        rs.count().coalesce(0L),
                        ac.codeName,
                        ac.code
                ))
                .from(acc)
                .join(ai).on(ai.accommodation.eq(acc)
                                             .and(ai.thumbnail.isTrue()))
                .join(ap).on(ap.accommodation.eq(acc)
                                             .and(ap.season.eq(season).and(ap.dayType.eq(dayType))))
                .join(acc.sigunguCode, sigunguCode)
                .join(sigunguCode.areaCode, ac)
                .leftJoin(wa).on(wa.accommodation.eq(acc)
                                                 .and(guestId != null ? wa.wishlist.guest.id.eq(guestId) : Expressions.FALSE))
                .leftJoin(rs).on(rs.accommodation.eq(acc))
                .leftJoin(rv).on(rv.reservation.eq(rs))
                .groupBy(acc.id, acc.title, ap.price, ai.imageUrl, wa, ac.codeName, ac.code)
                .orderBy(rs.count().desc())
                .fetch();
    }

    public Page<FilteredAccListResDto> getFilteredPagingAccommodations(AccSearchCondDto searchDto,
                                                                       Long guestId, Pageable pageable,
                                                                       Season season, DayType dayType) {
        QAccommodation acc = accommodation;
        QAccommodationImage ai = accommodationImage;
        QAccommodationPrice ap = accommodationPrice;
        QReservation rs = reservation;
        QReview rv = review;
        QAreaCode ac = areaCode;
        QWishlistAccommodation wa = wishlistAccommodation;

        //이미지 목록 제외 필드 조회
        List<Tuple> tuples = queryFactory
                .select(acc.id, acc.title, ap.price,
                        rv.rating.avg().coalesce(0.0),
                        rv.count().intValue().coalesce(0),
                        isInWishlist(wa)
                )
                .from(acc)
                .leftJoin(ai).on(ai.accommodation.eq(acc))
                .leftJoin(rs).on(rs.accommodation.eq(acc))
                .leftJoin(rv).on(rv.reservation.eq(rs))
                .leftJoin(wa).on(wa.accommodation.eq(acc)
                                                 .and(guestId != null ? wa.wishlist.guest.id.eq(guestId) : Expressions.FALSE))
                .join(ap).on(ap.accommodation.eq(acc)
                                             .and(ap.season.eq(season).and(ap.dayType.eq(dayType))))
                .join(acc.sigunguCode, sigunguCode)
                .join(sigunguCode.areaCode, ac)
                .where(
                        eqAreaCode(searchDto.areaCode()),
                        goePrice(searchDto.priceGoe()),
                        loePrice(searchDto.priceLoe()),
                        hasAllAmenities(searchDto.amenities())
                )
                .groupBy(acc.id, acc.title, ap.price, wa)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        //in절로 조회된 숙소의 이미지 목록 조회(전체)
        List<Long> accIds = tuples.stream().map(t -> t.get(acc.id)).toList();
        List<Tuple> imageTuples = queryFactory.select(ai.accommodation.id, ai.imageUrl)
                                              .from(ai)
                                              .where(ai.accommodation.id.in(accIds))
                                              .orderBy(ai.id.desc())
                                              .fetch();

        //직접 숙소당 최대 10개 이미지 목록 매핑
        Map<Long, List<String>> imagesMap = imageTuples
                .stream()
                .collect(groupingBy(
                        t -> t.get(ai.accommodation.id),
                        mapping(
                                t -> t.get(ai.imageUrl),
                                collectingAndThen(
                                        toList(),
                                        list -> list.stream().limit(10).toList()
                                )
                        )
                ));

        //응답 DTO 매핑
        List<FilteredAccListResDto> content = tuples
                .stream()
                .map(t -> {
                    Long accommodationId = t.get(acc.id);
                    return new FilteredAccListResDto(
                            accommodationId,
                            t.get(acc.title),
                            t.get(ap.price),
                            t.get(rv.rating.avg().coalesce(0.0)),
                            t.get(rv.count().intValue().coalesce(0)),
                            imagesMap.getOrDefault(accommodationId, List.of()),
                            t.get(5, Boolean.class)
                    );
                })
                .toList();

        //카운트쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(acc.count())
                .from(acc)
                .join(ap).on(ap.accommodation.eq(acc)
                                             .and(ap.season.eq(season)).and(ap.dayType.eq(dayType)))
                .join(acc.sigunguCode, sigunguCode)
                .join(sigunguCode.areaCode, ac)
                .where(
                        eqAreaCode(searchDto.areaCode()),
                        goePrice(searchDto.priceGoe()),
                        loePrice(searchDto.priceLoe()),
                        hasAllAmenities(searchDto.amenities())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    public Optional<DetailAccommodationQueryDto> findAccommodation(Long accId, Long guestId, Season season, DayType dayType) {
        QAccommodation acc = accommodation;
        QAccommodationPrice ap = accommodationPrice;
        QWishlistAccommodation wa = wishlistAccommodation;

        return Optional.ofNullable(
                queryFactory.select(constructor(DetailAccommodationQueryDto.class,
                                    acc.id, acc.title, acc.maxPeople, acc.address,
                                    acc.mapX, acc.mapY, acc.checkIn, acc.checkOut, acc.description,
                                    acc.number, acc.refundRegulation, ap.price,
                                    isInWishlist(wa),
                                    avgRateSubquery(accId)
                            ))
                            .from(acc)
                            .join(ap).on(ap.accommodation.eq(acc)
                                                         .and(ap.season.eq(season).and(ap.dayType.eq(dayType))))
                            .leftJoin(wa).on(wa.accommodation.eq(acc)
                                                             .and(guestId != null ? wa.wishlist.guest.id.eq(guestId) : Expressions.FALSE))
                            .where(acc.id.eq(accId))
                            .fetchOne()
        );
    }

    public List<ImageDataQueryDto> findImages(Long accId) {
        QAccommodationImage ai = accommodationImage;
        return queryFactory
                .select(constructor(ImageDataQueryDto.class,
                        ai.thumbnail,
                        ai.imageUrl
                ))
                .from(ai)
                .where(ai.accommodation.id.eq(accId))
                .fetch();
    }

    public List<String> findAmenities(Long accId) {
        QAccommodationAmenity aa = accommodationAmenity;
        QAmenity am = amenity;
        return queryFactory
                .select(am.description)
                .from(aa)
                .join(aa.amenity, am)
                .where(aa.accommodation.id.eq(accId))
                .fetch();
    }

    public List<DetailReviewDto> findReviews(Long accId) {
        QGuest g = guest;
        QReview rv = review;
        QReservation rs = reservation;

        return queryFactory
                .select(constructor(DetailReviewDto.class,
                        g.id,
                        g.name,
                        g.profileUrl,
                        g.createdAt,
                        rv.createdAt,
                        rv.rating,
                        rv.content
                ))
                .from(rs)
                .join(rv).on(rv.reservation.eq(rs))
                .join(rv.guest, g)
                .where(rs.accommodation.id.eq(accId))
                .orderBy(rv.createdAt.desc())
                .fetch();
    }

    private JPQLQuery<Double> avgRateSubquery(Long accId) {
        QReservation rs = reservation;
        QReview rv = review;
        return JPAExpressions.select(rv.rating.avg().coalesce(0.0))
                             .from(rv)
                             .join(rv.reservation, rs)
                             .where(rs.accommodation.id.eq(accId));
    }

    private BooleanExpression isInWishlist(QWishlistAccommodation wa) {
        return new CaseBuilder().when(wa.isNotNull())
                                .then(true)
                                .otherwise(false);
    }

    private BooleanExpression eqAreaCode(String code) {
        return hasText(code) ? areaCode.code.eq(code) : null;
    }

    private BooleanExpression goePrice(Integer price) {
        return (price != null) ? accommodationPrice.price.goe(price) : null;
    }

    private BooleanExpression loePrice(Integer price) {
        return (price != null) ? accommodationPrice.price.loe(price) : null;
    }

    private BooleanExpression hasAllAmenities(List<String> amenities) {
        if (amenities == null || amenities.isEmpty()) {
            return null;
        }

        QAccommodationAmenity aa = accommodationAmenity;

        return JPAExpressions
                .select(aa.amenity.countDistinct())
                .from(aa)
                .join(aa.amenity, amenity)
                .where(aa.accommodation.eq(accommodation),
                        amenity.name.in(amenities))
                .eq((long) amenities.size());
    }
}
