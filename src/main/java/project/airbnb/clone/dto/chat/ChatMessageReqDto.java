package project.airbnb.clone.dto.chat;

public record ChatMessageReqDto(
        Long senderId,
        String content) {
}
