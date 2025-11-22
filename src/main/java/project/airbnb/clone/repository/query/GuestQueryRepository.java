package project.airbnb.clone.repository.query;

import com.querydsl.core.types.Projections;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import project.airbnb.clone.dto.guest.ChatGuestSearchDto;
import project.airbnb.clone.dto.guest.TripHistoryResDto;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.repository.dto.DefaultProfileQueryDto;
import project.airbnb.clone.repository.query.support.CustomQuerydslRepositorySupport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static project.airbnb.clone.entity.QAccommodation.accommodation;
import static project.airbnb.clone.entity.QAccommodationImage.accommodationImage;
import static project.airbnb.clone.entity.QGuest.guest;
import static project.airbnb.clone.entity.QReservation.reservation;
import static project.airbnb.clone.entity.QReview.review;

@Repository
public class GuestQueryRepository extends CustomQuerydslRepositorySupport {

    public GuestQueryRepository() {
        super(Guest.class);
    }

    public Optional<DefaultProfileQueryDto> getDefaultProfile(Long guestId) {
        return Optional.ofNullable(
                select(Projections.constructor(
                        DefaultProfileQueryDto.class,
                        guest.name,
                        guest.profileUrl,
                        guest.createdAt,
                        guest.aboutMe,
                        guest.isEmailVerified))
                        .from(guest)
                        .where(guest.id.eq(guestId))
                        .fetchOne()
        );
    }

    public List<ChatGuestSearchDto> findGuestsByName(String name) {
        return select(Projections.constructor(
                ChatGuestSearchDto.class,
                guest.id,
                guest.name,
                guest.createdAt,
                guest.profileUrl))
                .from(guest)
                .where(guest.name.contains(name))
                .fetch();
    }

    public Page<TripHistoryResDto> getTripsHistory(Long guestId, Pageable pageable) {
        return applyPagination(pageable,
                contentQuery ->
                        contentQuery.select(Projections.constructor(
                                            TripHistoryResDto.class,
                                            reservation.id,
                                            accommodation.id,
                                            accommodationImage.imageUrl,
                                            accommodation.title,
                                            reservation.startDate,
                                            reservation.endDate,
                                            review.isNotNull()))
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
                                    .orderBy(reservation.id.desc())
                ,
                countQuery -> countQuery.select(reservation.count())
                                        .from(reservation)
                                        .where(
                                                reservation.guest.id.eq(guestId),
                                                reservation.endDate.before(LocalDateTime.now())
                                        )
        );
    }
}
