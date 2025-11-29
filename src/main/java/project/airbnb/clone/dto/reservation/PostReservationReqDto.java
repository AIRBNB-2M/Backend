package project.airbnb.clone.dto.reservation;

import java.time.LocalDate;

public record PostReservationReqDto(
        LocalDate startDate,
        LocalDate endDate,
        int adults,
        int children,
        int infants
) {
}
