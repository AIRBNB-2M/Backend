package project.airbnb.clone.dto.chat;

import java.util.List;

public record ChatMessagesResDto(
        List<ChatMessageResDto> messages,
        boolean hasMore) {
}
