package project.airbnb.clone.dto.chat;

import java.time.LocalDateTime;

public record ChatRoomResDto(
        Long roomId,
        String customRoomName,
        Long memberId,
        String memberName,
        String memberProfileImage,
        boolean isOtherMemberActive,
        String lastMessage,
        LocalDateTime lastMessageTime,
        int unreadCount) {
}
