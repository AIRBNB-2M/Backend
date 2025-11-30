package project.airbnb.clone.dto.chatbot;

import jakarta.validation.constraints.NotBlank;

public record ChatbotReqDto(@NotBlank String message) {
}
