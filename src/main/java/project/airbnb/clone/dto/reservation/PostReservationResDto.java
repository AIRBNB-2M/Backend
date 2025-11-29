package project.airbnb.clone.dto.reservation;

import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.entity.Reservation;

import java.time.LocalDateTime;

public record PostReservationResDto(
        Long reservationId,
        String thumbnailUrl,
        String title,
        String refundRegulation,
        LocalDateTime startDate,
        LocalDateTime endDate,
        int adults,
        int children,
        int infants
) {
    public static PostReservationResDto of(Accommodation accommodation, String thumbnailUrl, Reservation reservation) {
        return new PostReservationResDto(reservation.getId(), thumbnailUrl, accommodation.getTitle(), accommodation.getRefundRegulation(),
                reservation.getStartDate(), reservation.getEndDate(), reservation.getAdults(), reservation.getChildren(), reservation.getInfants());
    }
}
