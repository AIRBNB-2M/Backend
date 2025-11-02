package project.airbnb.clone.common.events.chat;

import project.airbnb.clone.dto.chat.ChatRoomResDto;

public record ChatRequestAcceptedEvent(String requestId, Long senderId, ChatRoomResDto chatRoomResDto) {
}
