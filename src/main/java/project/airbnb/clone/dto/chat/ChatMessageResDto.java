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
        LocalDateTime timestamp) {
}
