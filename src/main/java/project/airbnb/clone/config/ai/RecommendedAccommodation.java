package project.airbnb.clone.config.ai;

public record RecommendedAccommodation(
        Long id,
        String title,
        String price,
        int maxPeople
) { }
