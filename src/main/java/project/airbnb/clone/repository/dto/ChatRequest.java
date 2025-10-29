package project.airbnb.clone.repository.dto;


import lombok.Builder;
import project.airbnb.clone.dto.chat.RequestChatResDto;

import java.time.LocalDateTime;

@Builder
public record ChatRequest(
        Long senderId,
        String senderName,
        String senderProfileImage,
        Long receiverId,
        String receiverName,
        String receiverProfileImage,
        LocalDateTime createdAt,
        LocalDateTime expiresAt) {

    public RequestChatResDto toResDto(String requestId) {
        return RequestChatResDto.builder()
                                .requestId(requestId)
                                .senderId(senderId)
                                .senderName(senderName)
                                .senderProfileImage(senderProfileImage)
                                .receiverId(receiverId)
                                .receiverName(receiverName)
                                .receiverProfileImage(receiverProfileImage)
                                .expiresAt(expiresAt)
                                .build();
    }
}
