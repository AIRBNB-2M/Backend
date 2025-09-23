package project.airbnb.clone.repository.dto;

public record MainAccListQueryDto(
        Long accommodationId,
        String title,
        int price,
        double avgRate,
        String thumbnailUrl,
        boolean isInWishlist,
        Long wishlistId,
        String wishlistName,
        long reservationCount,
        String areaName,
        String areaCode) {

    public AreaKey getAreaKey() {
        return new AreaKey(areaName, areaCode);
    }

    public record AreaKey(String areaName, String areaCode) {
    }
}
