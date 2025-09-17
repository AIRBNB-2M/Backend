package project.airbnb.clone.dto.wishlist;

import project.airbnb.clone.repository.dto.WishlistDetailQueryDto;

import java.util.List;

public record WishlistDetailResDto(
        Long accommodationId,
        String wishlistName,
        String title,
        String description,
        double mapX,
        double mapY,
        double avgRate,
        List<String> imageUrls,
        String memo) {

    public static WishlistDetailResDto from(WishlistDetailQueryDto queryDto, List<String> imageUrls) {
        return new WishlistDetailResDto(
                queryDto.accommodationId(),
                queryDto.wishlistName(),
                queryDto.title(),
                queryDto.description(),
                queryDto.mapX(),
                queryDto.mapY(),
                queryDto.avgRate(),
                imageUrls,
                queryDto.memo()
        );
    }
}
