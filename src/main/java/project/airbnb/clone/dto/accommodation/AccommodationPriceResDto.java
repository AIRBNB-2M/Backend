package project.airbnb.clone.dto.accommodation;

import java.time.LocalDate;

public record AccommodationPriceResDto(
        Long accommodationId,
        LocalDate date,
        int price) {
}
