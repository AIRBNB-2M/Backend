package project.airbnb.clone.entity.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.airbnb.clone.entity.BaseEntity;
import project.airbnb.clone.entity.Guest;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_participants",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"chat_room_id", "guest_id"})
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
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @Column(name = "is_creator", nullable = false)
    private Boolean isCreator;

    @Column(name = "custom_room_name", nullable = false)
    private String customRoomName;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "last_rejoined_at")
    private LocalDateTime lastRejoinedAt;

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
}
