package project.airbnb.clone.repository.jpa;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import project.airbnb.clone.entity.chat.ChatRoom;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
                SELECT cp1.chatRoom
                FROM ChatParticipant cp1
                JOIN ChatParticipant cp2 ON cp1.chatRoom = cp2.chatRoom
                WHERE cp1.guest.id = :guestId1
                AND cp2.guest.id = :guestId2
            """)
    Optional<ChatRoom> findByGuestsId(@Param("guestId1") Long guestId1, @Param("guestId2") Long guestId2);
}