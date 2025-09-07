package project.airbnb.clone.repository.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.airbnb.clone.consts.DayType;
import project.airbnb.clone.consts.Season;
import project.airbnb.clone.dto.accommodation.MainAccListQueryDto;
import project.airbnb.clone.entity.QAccommodation;
import project.airbnb.clone.entity.QAccommodationImage;
import project.airbnb.clone.entity.QAccommodationPrice;
import project.airbnb.clone.entity.QAreaCode;
import project.airbnb.clone.entity.QReservation;
import project.airbnb.clone.entity.QReview;

import java.util.List;

import static com.querydsl.core.types.Projections.constructor;
import static com.querydsl.core.types.dsl.Expressions.asBoolean;
import static project.airbnb.clone.entity.QAccommodation.accommodation;
import static project.airbnb.clone.entity.QAccommodationImage.accommodationImage;
import static project.airbnb.clone.entity.QAccommodationPrice.accommodationPrice;
import static project.airbnb.clone.entity.QAreaCode.areaCode;
import static project.airbnb.clone.entity.QLike.like;
import static project.airbnb.clone.entity.QReservation.reservation;
import static project.airbnb.clone.entity.QReview.review;
import static project.airbnb.clone.entity.QSigunguCode.sigunguCode;

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

        return queryFactory
                .select(constructor(MainAccListQueryDto.class,
                        acc.id,
                        acc.title,
                        ap.price,
                        rv.rating.avg().coalesce(0.0),
                        ai.imageUrl,
                        getLikeExists(guestId),
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
                .leftJoin(rs).on(rs.accommodation.eq(acc))
                .leftJoin(rv).on(rv.reservation.eq(rs))
                .groupBy(acc.id, acc.title, ap.price, ai.imageUrl, ac.codeName)
                .orderBy(rs.count().desc())
                .fetch();
    }

    private BooleanExpression getLikeExists(Long guestId) {
        return (guestId != null)
                ? JPAExpressions.selectOne()
                                .from(like)
                                .where(like.accommodation.eq(accommodation).and(like.guest.id.eq(guestId)))
                                .exists()
                : asBoolean(false);
    }
}
