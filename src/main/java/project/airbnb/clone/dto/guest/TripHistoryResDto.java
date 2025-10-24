package project.airbnb.clone.dto.guest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TripHistoryResDto(
        Long reservationId,
        Long accommodationId,
        String thumbnailUrl,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        boolean hasReviewed) {

    public TripHistoryResDto(Long reservationId, Long accommodationId, String thumbnailUrl, String title, LocalDateTime startDate, LocalDateTime endDate, boolean hasReviewed) {
        this(reservationId, accommodationId, thumbnailUrl, title, startDate.toLocalDate(), endDate.toLocalDate(), hasReviewed);
    }
}
