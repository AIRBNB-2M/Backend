package project.airbnb.clone.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.airbnb.clone.entity.chat.ChatRoom;
import project.airbnb.clone.entity.chat.ReadStatus;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, Long> {

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE ReadStatus rs
            SET rs.isRead = true
            WHERE rs.chatRoom = :chatRoom
            AND rs.guest.id = :creatorId
            """)
    void markAllIsRead(@Param("chatRoom") ChatRoom chatRoom, @Param("creatorId") Long creatorId);
}