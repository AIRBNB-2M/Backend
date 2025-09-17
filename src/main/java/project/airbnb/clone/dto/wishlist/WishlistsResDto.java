package project.airbnb.clone.dto.wishlist;

public record WishlistsResDto(
        Long wishlistId,
        String name,
        String thumbnailUrl,
        int savedAccommodations) {
}
