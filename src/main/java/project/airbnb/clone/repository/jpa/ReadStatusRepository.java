package project.airbnb.clone.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.airbnb.clone.entity.chat.ReadStatus;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, Long> {

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE ReadStatus rs
            SET rs.isRead = true
            WHERE rs.chatRoom.id = :roomId
            AND rs.guest.id = :guestId
            """)
    void markAllIsRead(@Param("roomId") Long roomId, @Param("guestId") Long guestId);
}