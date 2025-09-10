package project.airbnb.clone.dto.accommodation;

public record MainAccListQueryDto(
        Long accommodationId,
        String title,
        int price,
        double avgRate,
        String thumbnailUrl,
        boolean likedMe,
        long reservationCount,
        String areaName,
        String areaCode) {

    public AreaKey getAreaKey() {
        return new AreaKey(areaName, areaCode);
    }

    public record AreaKey(String areaName, String areaCode) {
    }
}
