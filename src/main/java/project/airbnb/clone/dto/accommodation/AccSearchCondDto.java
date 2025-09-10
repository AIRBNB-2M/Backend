package project.airbnb.clone.dto.accommodation;

import java.util.List;

public record AccSearchCondDto(
        String areaCode,
        List<String> amenities,
        Integer priceGoe,
        Integer priceLoe) {
}
