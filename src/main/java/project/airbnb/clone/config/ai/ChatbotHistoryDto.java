package project.airbnb.clone.config.ai;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import project.airbnb.clone.entity.chat.ChatbotHistory;

import java.time.LocalDateTime;

public record ChatbotHistoryDto(
        MessageType type,
        String text,
        LocalDateTime createdAt) {

    public static ChatbotHistoryDto of(Message message) {
        return new ChatbotHistoryDto(message.getMessageType(), message.getText(), LocalDateTime.now());
    }

    public static ChatbotHistoryDto of(ChatbotHistory chatbotHistory) {
        return new ChatbotHistoryDto(chatbotHistory.getType(), chatbotHistory.getText(), chatbotHistory.getCreatedAt());
    }
}
