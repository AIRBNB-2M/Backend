package project.airbnb.clone.repository.dto;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import project.airbnb.clone.dto.chat.RequestChatResDto;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@RedisHash(value = "chatRequest", timeToLive = 86400)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatRequest {
    @Id
    String requestId;

    @Indexed
    Long senderId;
    String senderName;
    String senderProfileImage;

    @Indexed
    Long receiverId;
    String receiverName;
    String receiverProfileImage;

    LocalDateTime createdAt;
    LocalDateTime expiresAt;

    public RequestChatResDto toResDto() {
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
