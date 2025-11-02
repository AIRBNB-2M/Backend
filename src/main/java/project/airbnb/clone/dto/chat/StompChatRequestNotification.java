package project.airbnb.clone.dto.chat;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record StompChatRequestNotification(
        String requestId,
        Long senderId,
        String senderName,
        String senderProfileImage,
        LocalDateTime expiresAt
) {}
