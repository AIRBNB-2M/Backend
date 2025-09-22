package project.airbnb.clone.repository.dto;

public record FilteredAccListQueryDto(
        Long accommodationId,
        String title,
        int price,
        double avgRate,
        int reviewCount,
        boolean isInWishlist,
        Long wishlistId,
        String wishlistName) {
}
