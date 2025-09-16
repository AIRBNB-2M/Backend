package project.airbnb.clone.dto.accommodation;

import java.util.List;

public record FilteredAccListResDto(
        Long accommodationId,
        String title,
        int price,
        double avgRate,
        int avgCount,
        List<String> imageUrls,
        boolean isInWishlist,
        Long wishlistId) {
}
