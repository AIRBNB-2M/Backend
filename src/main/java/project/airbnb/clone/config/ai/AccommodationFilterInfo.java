package project.airbnb.clone.config.ai;

import java.util.List;

public record AccommodationFilterInfo(
        String region,
        Integer minPrice,
        Integer maxPrice,
        Integer peopleCount,
        List<String> amenities
) {
}
