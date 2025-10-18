package project.airbnb.clone.repository.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
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
import project.airbnb.clone.dto.accommodation.ViewHistoryDto;
import project.airbnb.clone.repository.dto.AccAllImagesQueryDto;
import project.airbnb.clone.repository.dto.DetailAccommodationQueryDto;
import project.airbnb.clone.repository.dto.FilteredAccListQueryDto;
import project.airbnb.clone.repository.dto.ImageDataQueryDto;
import project.airbnb.clone.repository.dto.MainAccListQueryDto;

import java.time.LocalDateTime;
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
import static project.airbnb.clone.entity.QViewHistory.viewHistory;
import static project.airbnb.clone.entity.QWishlist.wishlist;
import static project.airbnb.clone.entity.QWishlistAccommodation.wishlistAccommodation;

@Repository
@RequiredArgsConstructor
public class AccommodationQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<MainAccListQueryDto> getAreaAccommodations(Season season, DayType dayType, Long guestId) {
        return new AccommodationQueryBuilder(queryFactory, dayType, season, guestId)
                .fetchMainAccList();
    }

    public Page<FilteredAccListResDto> getFilteredPagingAccommodations(AccSearchCondDto searchDto,
                                                                       Long guestId, Pageable pageable,
                                                                       Season season, DayType dayType) {
        //이미지 목록 제외 필드 조회
        List<FilteredAccListQueryDto> queryDtos = new AccommodationQueryBuilder(queryFactory, dayType, season, guestId)
                .fetchFilteredAccList(pageable,
                        eqAreaCode(searchDto.areaCode()),
                        goePrice(searchDto.priceGoe()),
                        loePrice(searchDto.priceLoe()),
                        hasAllAmenities(searchDto.amenities())
                );

        //in절로 조회된 숙소의 이미지 목록 조회(전체)
        List<Long> accIds = queryDtos.stream().map(FilteredAccListQueryDto::accommodationId).toList();
        List<AccAllImagesQueryDto> imagesQueryDtos = queryFactory.select(constructor(AccAllImagesQueryDto.class,
                                                                         accommodationImage.accommodation.id, accommodationImage.imageUrl))
                                                                 .from(accommodationImage)
                                                                 .where(accommodationImage.accommodation.id.in(accIds))
                                                                 .orderBy(accommodationImage.id.desc())
                                                                 .fetch();
        //직접 숙소당 최대 10개 이미지 목록 매핑
        Map<Long, List<String>> imagesMap = imagesQueryDtos.stream()
                                                           .collect(groupingBy(
                                                                   AccAllImagesQueryDto::accommodationId,
                                                                   mapping(
                                                                           AccAllImagesQueryDto::imageUrl,
                                                                           collectingAndThen(toList(), list -> list.stream()
                                                                                                                   .limit(10)
                                                                                                                   .toList())
                                                                   )
                                                           ));
        //응답 DTO 매핑
        List<FilteredAccListResDto> content = queryDtos.stream()
                                                       .map(dto -> FilteredAccListResDto.from(dto, imagesMap.getOrDefault(dto.accommodationId(), List.of())))
                                                       .toList();

        //카운트쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(accommodation.count())
                .from(accommodation)
                .join(accommodationPrice).on(accommodationPrice.accommodation.eq(accommodation)
                                                                             .and(accommodationPrice.season.eq(season))
                                                                             .and(accommodationPrice.dayType.eq(dayType)))
                .join(accommodation.sigunguCode, sigunguCode)
                .join(sigunguCode.areaCode, areaCode)
                .where(
                        eqAreaCode(searchDto.areaCode()),
                        goePrice(searchDto.priceGoe()),
                        loePrice(searchDto.priceLoe()),
                        hasAllAmenities(searchDto.amenities())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    public Optional<DetailAccommodationQueryDto> findAccommodation(Long accId, Long guestId, Season season, DayType dayType) {
        return new AccommodationQueryBuilder(queryFactory, dayType, season, guestId)
                .fetchDetailAcc(accId);
    }

    public List<ImageDataQueryDto> findImages(Long accId) {
        return queryFactory
                .select(constructor(ImageDataQueryDto.class,
                        accommodationImage.thumbnail,
                        accommodationImage.imageUrl
                ))
                .from(accommodationImage)
                .where(accommodationImage.accommodation.id.eq(accId))
                .fetch();
    }

    public List<String> findAmenities(Long accId) {
        return queryFactory
                .select(amenity.description)
                .from(accommodationAmenity)
                .join(accommodationAmenity.amenity, amenity)
                .where(accommodationAmenity.accommodation.id.eq(accId))
                .fetch();
    }

    public List<DetailReviewDto> findReviews(Long accId) {
        return queryFactory
                .select(constructor(DetailReviewDto.class,
                        guest.id,
                        guest.name,
                        guest.profileUrl,
                        guest.createdAt,
                        review.createdAt,
                        review.rating,
                        review.content
                ))
                .from(reservation)
                .join(review).on(review.reservation.eq(reservation))
                .join(review.guest, guest)
                .where(reservation.accommodation.id.eq(accId))
                .orderBy(review.createdAt.desc())
                .fetch();
    }

    public List<ViewHistoryDto> findViewHistories(Long guestId) {
        return queryFactory
                .select(constructor(ViewHistoryDto.class,
                        viewHistory.viewedAt,
                        accommodation.id,
                        accommodation.title,
                        review.rating.avg().coalesce(0.0),
                        accommodationImage.imageUrl,
                        wishlist.isNotNull(),
                        wishlist.id,
                        wishlist.name
                ))
                .from(viewHistory)
                .join(viewHistory.accommodation, accommodation)

                .join(accommodationImage).on(accommodationImage.accommodation.eq(accommodation)
                                                                             .and(accommodationImage.thumbnail.isTrue()))

                .leftJoin(wishlistAccommodation).on(wishlistAccommodation.accommodation.eq(accommodation))
                .leftJoin(wishlistAccommodation.wishlist, wishlist).on(wishlist.guest.id.eq(guestId))

                .leftJoin(reservation).on(reservation.accommodation.eq(accommodation))
                .leftJoin(review).on(review.reservation.eq(reservation))

                .where(viewHistory.guest.id.eq(guestId).and(viewHistory.viewedAt.after(LocalDateTime.now().minusDays(30))))

                .groupBy(viewHistory.viewedAt, accommodation.id, accommodation.title, accommodationImage.imageUrl, wishlist.id, wishlist.name)
                .orderBy(viewHistory.viewedAt.desc())
                .fetch();
    }

    public Integer getAccommodationPrice(Long accId, Season season, DayType dayType) {
        return queryFactory.select(accommodationPrice.price)
                           .from(accommodationPrice)
                           .where(accommodationPrice.accommodation.id.eq(accId)
                                                                     .and(accommodationPrice.season.eq(season))
                                                                     .and(accommodationPrice.dayType.eq(dayType)))
                           .fetchOne();
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

        return JPAExpressions
                .select(accommodationAmenity.amenity.countDistinct())
                .from(accommodationAmenity)
                .join(accommodationAmenity.amenity, amenity)
                .where(accommodationAmenity.accommodation.eq(accommodation),
                        amenity.name.in(amenities))
                .eq((long) amenities.size());
    }
}
