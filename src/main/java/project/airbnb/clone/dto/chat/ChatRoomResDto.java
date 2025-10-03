package project.airbnb.clone.dto.chat;

import java.time.LocalDateTime;

public record ChatRoomResDto(
        Long roomId,
        String customRoomName,
        Long guestId,
        String guestName,
        String guestProfileImage,
        String lastMessage,
        LocalDateTime lastMessageTime,
        int unreadCount) {
}
