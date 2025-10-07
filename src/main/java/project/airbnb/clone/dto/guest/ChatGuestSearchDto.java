package project.airbnb.clone.dto.guest;

import java.time.LocalDateTime;

public record ChatGuestSearchDto(
        Long id,
        String name,
        LocalDateTime createdDateTime,
        String profileImageUrl) {
}
