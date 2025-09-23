package project.airbnb.clone.dto.accommodation;

import java.time.LocalDate;
import java.util.List;

public record ViewHistoryResDto(
        LocalDate date,
        List<ViewHistoryDto> accommodations) {
}
