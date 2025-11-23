package project.airbnb.clone.entity.chat;

import jakarta.persistence.*;
import lombok.Getter;
import project.airbnb.clone.entity.BaseEntity;

@Entity
@Getter
@Table(name = "chat_rooms")
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id", nullable = false)
    private Long id;
}
