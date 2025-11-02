package project.airbnb.clone.dto.chat;

import lombok.Builder;

@Builder
public record StompChatRequestResponseNotification(
        String requestId,
        boolean accepted,
        Long roomId,
        String message,
        ChatRoomResDto chatRoom
) {}