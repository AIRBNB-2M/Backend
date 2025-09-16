package project.airbnb.clone.dto.accommodation;

import project.airbnb.clone.repository.dto.DetailAccommodationQueryDto;

import java.time.LocalDateTime;
import java.util.List;

public record DetailAccommodationResDto(
        Long accommodationId,
        String title,
        int maxPeople,
        String address,
        double mapX,
        double mapY,
        String checkIn,
        String checkOut,
        String description,
        String number,
        String refundRegulation,

        int price,
        boolean isInWishlist,
        Long wishlistId,
        double avgRate,
        DetailImageDto images,
        List<String> amenities,
        List<DetailReviewDto> reviews) {

    public record DetailImageDto(
            String thumbnail,
            List<String> others) {
    }

    public record DetailReviewDto(
            Long guestId,
            String guestName,
            String profileUrl,
            LocalDateTime guestCreatedDate,
            LocalDateTime reviewCreatedDate,
            double rating,
            String content) {
    }

    public static DetailAccommodationResDto from(DetailAccommodationQueryDto queryDto,
                                                 DetailImageDto imageDto,
                                                 List<String> amenities,
                                                 List<DetailReviewDto> reviewDtos) {
        return new DetailAccommodationResDto(
                queryDto.accommodationId(),
                queryDto.title(),
                queryDto.maxPeople(),
                queryDto.address(),
                queryDto.mapX(),
                queryDto.mapY(),
                queryDto.checkIn(),
                queryDto.checkOut(),
                queryDto.description(),
                queryDto.number(),
                queryDto.refundRegulation(),
                queryDto.price(),
                queryDto.isInWishlist(),
                queryDto.wishlistId(),
                queryDto.avgRate(),
                imageDto,
                amenities,
                reviewDtos
        );
    }
}
