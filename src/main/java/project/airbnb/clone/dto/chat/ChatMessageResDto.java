package project.airbnb.clone.dto.chat;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatMessageResDto(
        Long messageId,
        Long roomId,
        Long senderId,
        String senderName,
        String content,
        LocalDateTime timestamp,
        boolean isLeft) {

    public ChatMessageResDto(Long messageId, Long roomId, Long senderId, String senderName, String content, LocalDateTime timestamp) {
        this(messageId, roomId, senderId, senderName, content, timestamp, false);
    }
}
