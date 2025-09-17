package project.airbnb.clone.dto.wishlist;

import jakarta.validation.constraints.Size;

public record MemoUpdateReqDto(@Size(max = 250) String memo) {
}
