package project.airbnb.clone.config.ai;

public record AccommodationFilterInfo(
        String region,
        Integer minPrice,
        Integer maxPrice,
        Integer peopleCount
) {
}
