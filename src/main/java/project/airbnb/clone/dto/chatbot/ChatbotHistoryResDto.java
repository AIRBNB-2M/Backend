package project.airbnb.clone.dto.chatbot;

import org.springframework.ai.chat.messages.MessageType;

public record ChatbotHistoryResDto(
        String content,
        MessageType messageType
) {
}
