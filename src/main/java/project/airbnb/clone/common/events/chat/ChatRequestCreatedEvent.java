package project.airbnb.clone.common.events.chat;

import project.airbnb.clone.repository.dto.redis.ChatRequest;

public record ChatRequestCreatedEvent(ChatRequest chatRequest) {
}
