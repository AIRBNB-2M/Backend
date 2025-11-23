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
                WHERE cp1.member.id = :currentMemberId
                AND cp2.member.id = :otherMemberId
            """)
    Optional<ChatRoom> findByMembersId(@Param("currentMemberId") Long currentMemberId, @Param("otherMemberId") Long otherMemberId);
}