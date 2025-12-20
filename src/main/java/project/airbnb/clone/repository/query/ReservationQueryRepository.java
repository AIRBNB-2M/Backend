package project.airbnb.clone.repository.query;

import org.springframework.stereotype.Repository;
import project.airbnb.clone.entity.accommodation.Accommodation;
import project.airbnb.clone.entity.reservation.Reservation;
import project.airbnb.clone.repository.query.support.CustomQuerydslRepositorySupport;

import java.time.LocalDateTime;

import static project.airbnb.clone.consts.ReservationStatus.CONFIRMED;
import static project.airbnb.clone.entity.QAccommodation.accommodation;
import static project.airbnb.clone.entity.QReservation.reservation;

@Repository
public class ReservationQueryRepository extends CustomQuerydslRepositorySupport {

    public ReservationQueryRepository() {
        super(Reservation.class);
    }

    public boolean existsConfirmedReservation(Accommodation acc, LocalDateTime from, LocalDateTime to) {
        return getQueryFactory()
                .selectOne()
                .from(reservation)
                .join(reservation.accommodation, accommodation)
                .where(
                        reservation.accommodation.eq(acc),
                        reservation.status.eq(CONFIRMED),
                        reservation.startDate.lt(to),
                        reservation.endDate.gt(from)
                )
                .fetchFirst() != null;
    }
}
