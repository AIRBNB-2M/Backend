package project.airbnb.clone.entity.chat;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.airbnb.clone.entity.BaseEntity;
import project.airbnb.clone.entity.member.Member;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_participants",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"chat_room_id", "member_id"})
        })
public class ChatParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_participant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_read_message")
    private ChatMessage lastReadMessage;

    @Column(name = "is_creator", nullable = false)
    private Boolean isCreator;

    @Column(name = "custom_room_name", nullable = false)
    private String customRoomName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "last_rejoined_at")
    private LocalDateTime lastRejoinedAt;

    public static ChatParticipant creator(ChatRoom chatRoom, Member sender, String roomName) {
        return new ChatParticipant(chatRoom, sender, true, roomName);
    }

    public static ChatParticipant participant(ChatRoom chatRoom, Member receiver, String roomName) {
        return new ChatParticipant(chatRoom, receiver, false, roomName);
    }

    private ChatParticipant(ChatRoom chatRoom, Member member, Boolean isCreator, String customRoomName) {
        this.chatRoom = chatRoom;
        this.member = member;
        this.isCreator = isCreator;
        this.customRoomName = customRoomName;
    }

    public void leave() {
        if (!this.isActive) {
            return;
        }
        this.isActive = false;
        this.leftAt = LocalDateTime.now();
    }

    public boolean hasLeft() {
        return !isActive && leftAt != null;
    }

    public boolean isActiveParticipant() {
        return !hasLeft();
    }

    public void rejoin() {
        if (this.isActive) {
            return;
        }
        this.isActive = true;
        this.lastRejoinedAt = LocalDateTime.now();
    }

    public void updateLastReadMessage(ChatMessage lastReadMessage) {
        this.lastReadMessage = lastReadMessage;
    }
}
