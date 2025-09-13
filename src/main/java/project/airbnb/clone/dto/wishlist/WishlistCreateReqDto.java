package project.airbnb.clone.dto.wishlist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WishlistCreateReqDto(@NotBlank @Size(min = 1, max = 50) String wishlistName) {
}
