package project.airbnb.clone.repository.dto.redis;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import project.airbnb.clone.dto.chat.RequestChatResDto;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@RedisHash(value = "chatRequest", timeToLive = 86400)
public class ChatRequest {
    @Id
    private String requestId;

    @Indexed
    private Long senderId;
    private String senderName;
    private String senderProfileImage;

    @Indexed
    private Long receiverId;
    private String receiverName;
    private String receiverProfileImage;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

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
