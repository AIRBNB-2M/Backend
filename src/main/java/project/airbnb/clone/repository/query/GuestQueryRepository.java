package project.airbnb.clone.repository.query;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import project.airbnb.clone.dto.guest.ChatGuestSearchDto;
import project.airbnb.clone.dto.guest.TripHistoryResDto;
import project.airbnb.clone.repository.dto.DefaultProfileQueryDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static project.airbnb.clone.entity.QAccommodation.accommodation;
import static project.airbnb.clone.entity.QAccommodationImage.accommodationImage;
import static project.airbnb.clone.entity.QGuest.guest;
import static project.airbnb.clone.entity.QReservation.reservation;
import static project.airbnb.clone.entity.QReview.review;

@Repository
@RequiredArgsConstructor
public class GuestQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<DefaultProfileQueryDto> getDefaultProfile(Long guestId) {
        return Optional.ofNullable(
                queryFactory
                        .select(Projections.constructor(DefaultProfileQueryDto.class,
                                guest.name,
                                guest.profileUrl,
                                guest.createdAt,
                                guest.aboutMe,
                                guest.isEmailVerified
                        ))
                        .from(guest)
                        .where(guest.id.eq(guestId))
                        .fetchOne()
        );
    }

    public List<ChatGuestSearchDto> findGuestsByName(String name) {
        return queryFactory
                .select(Projections.constructor(ChatGuestSearchDto.class,
                        guest.id,
                        guest.name,
                        guest.createdAt,
                        guest.profileUrl
                ))
                .from(guest)
                .where(guest.name.contains(name))
                .fetch();
    }

    public Page<TripHistoryResDto> getTripsHistory(Long guestId, Pageable pageable) {
        List<TripHistoryResDto> content = queryFactory.select(Projections.constructor(TripHistoryResDto.class,
                                                              reservation.id,
                                                              accommodation.id,
                                                              accommodationImage.imageUrl,
                                                              accommodation.title,
                                                              reservation.startDate,
                                                              reservation.endDate,
                                                              review.isNotNull()
                                                      ))
                                                      .from(accommodation)
                                                      .join(accommodationImage)
                                                      .on(accommodationImage.accommodation.eq(accommodation)
                                                                                          .and(accommodationImage.thumbnail.isTrue()))
                                                      .leftJoin(reservation)
                                                      .on(reservation.accommodation.eq(accommodation))
                                                      .leftJoin(review).on(review.reservation.eq(reservation))
                                                      .where(
                                                              reservation.isNotNull(),
                                                              reservation.guest.id.eq(guestId),
                                                              reservation.endDate.before(LocalDateTime.now()))
                                                      .offset(pageable.getOffset())
                                                      .limit(pageable.getPageSize())
                                                      .orderBy(reservation.id.desc())
                                                      .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(reservation.count())
                                                .from(reservation)
                                                .where(
                                                        reservation.guest.id.eq(guestId),
                                                        reservation.endDate.before(LocalDateTime.now())
                                                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
