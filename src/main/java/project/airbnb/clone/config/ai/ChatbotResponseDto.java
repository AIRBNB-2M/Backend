package project.airbnb.clone.config.ai;

import java.util.Map;

public record ChatbotResponseDto(
        String textResponse,
        Map<String, Object> metadata
) {
}
