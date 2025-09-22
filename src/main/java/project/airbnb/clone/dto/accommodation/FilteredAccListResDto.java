package project.airbnb.clone.dto.accommodation;

import project.airbnb.clone.repository.dto.FilteredAccListQueryDto;

import java.util.List;

public record FilteredAccListResDto(
        Long accommodationId,
        String title,
        int price,
        double avgRate,
        int reviewCount,
        List<String> imageUrls,
        boolean isInWishlist,
        Long wishlistId,
        String wishlistName) {

    public static FilteredAccListResDto from(FilteredAccListQueryDto queryDto, List<String> imageUrls) {
        return new FilteredAccListResDto(
                queryDto.accommodationId(),
                queryDto.title(),
                queryDto.price(),
                queryDto.avgRate(),
                queryDto.reviewCount(),
                imageUrls,
                queryDto.isInWishlist(),
                queryDto.wishlistId(),
                queryDto.wishlistName()
        );
    }
}
