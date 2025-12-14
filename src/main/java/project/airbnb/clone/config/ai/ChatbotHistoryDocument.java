package project.airbnb.clone.config.ai;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Document(collection = "chatbot_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatbotHistoryDocument {

    @Id
    private String id;

    @Field("conversation_id")
    @Indexed
    private String conversationId;

    @Field("message_type")
    private MessageType messageType;

    @Field("content")
    private String content;

    @Field("metadata")
    private Map<String, Object> metadata;

    @Field("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    public static ChatbotHistoryDocument of(String conversationId, Message message) {
        return new ChatbotHistoryDocument(conversationId, message.getText(), message.getMetadata(), message.getMessageType());
    }

    private ChatbotHistoryDocument(String conversationId, String content, Map<String, Object> metadata, MessageType messageType) {
        this.conversationId = conversationId;
        this.content = content;
        this.metadata = metadata;
        this.messageType = messageType;
    }
}
