package project.airbnb.clone.config.ai.embed;

import project.airbnb.clone.consts.DayType;
import project.airbnb.clone.consts.Season;

public record AccommodationEmbeddingDto(
        Long accommodationId,
        String title,
        String description,
        int maxPeople,
        String address,
        String areaName,
        String sigunguName,
        Season season,
        DayType dayType,
        int price
) {
    public String getRegion() {
        return areaName + " " + sigunguName;
    }
}
