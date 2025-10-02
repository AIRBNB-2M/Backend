package project.airbnb.clone.dto.chat;

import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.entity.chat.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageResDto(
        Long messageId,
        Long roomId,
        Long senderId,
        String senderName,
        String content,
        LocalDateTime timestamp) {

    public static ChatMessageResDto from(ChatMessage message, Guest writer, Long roomId) {
        return new ChatMessageResDto(
                message.getId(),
                roomId,
                writer.getId(),
                writer.getName(),
                message.getContent(),
                message.getCreatedAt());
    }
}
