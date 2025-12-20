package project.airbnb.clone.entity.chat;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.airbnb.clone.entity.BaseEntity;
import project.airbnb.clone.entity.member.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_messages")
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member writer;

    @Column(name = "content", nullable = false)
    private String content;

    public static ChatMessage create(ChatRoom chatRoom, Member writer, String content) {
        return new ChatMessage(chatRoom, writer, content);
    }

    private ChatMessage(ChatRoom chatRoom, Member writer, String content) {
        this.chatRoom = chatRoom;
        this.writer = writer;
        this.content = content;
    }
}
