package project.airbnb.clone.common.events.chat;

import project.airbnb.clone.repository.dto.ChatRequest;

public record ChatRequestCreatedEvent(ChatRequest chatRequest) {
}
