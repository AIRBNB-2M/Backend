package project.airbnb.clone.repository.dto;

public record DetailAccommodationQueryDto(
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
        Double avgRate) {
}
