package project.airbnb.clone.entity.chat;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import project.airbnb.clone.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chatbot_histories")
public class ChatbotHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatbot_history_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType type;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "conversation_id", nullable = false)
    private String conversationId;

    public static ChatbotHistory of(String conversationId, Message message) {
        return new ChatbotHistory(message.getMessageType(), message.getText(), conversationId);
    }

    private ChatbotHistory(MessageType type, String text, String conversationId) {
        this.type = type;
        this.text = text;
        this.conversationId = conversationId;
    }
}
