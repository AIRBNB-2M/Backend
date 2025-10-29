package project.airbnb.clone.dto.chat;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RequestChatResDto(
        String requestId,
        Long senderId,
        String senderName,
        String senderProfileImage,
        Long receiverId,
        String receiverName,
        String receiverProfileImage,
        LocalDateTime expiresAt) {
}
