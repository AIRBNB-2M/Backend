package project.airbnb.clone.dto.guest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TripHistoryResDto(
        Long accommodationId,
        String thumbnailUrl,
        String title,
        LocalDate startDate,
        LocalDate endDate) {

    public TripHistoryResDto(Long accommodationId, String thumbnailUrl, String title, LocalDateTime startDate, LocalDateTime endDate) {
        this(accommodationId, thumbnailUrl, title, startDate.toLocalDate(), endDate.toLocalDate());
    }
}
