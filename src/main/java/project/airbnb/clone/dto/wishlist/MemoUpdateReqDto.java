package project.airbnb.clone.dto.wishlist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemoUpdateReqDto(@NotBlank @Size(min = 1, max = 250) String memo) {
}
