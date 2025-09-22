package project.airbnb.clone.dto.accommodation;

import java.time.LocalDateTime;

public record ViewHistoryDto(
        LocalDateTime viewDate,
        Long accommodationId,
        String title,
        double avgRate,
        String thumbnailUrl,
        boolean isInWishlist,
        Long wishlistId,
        String wishlistName) {
}
