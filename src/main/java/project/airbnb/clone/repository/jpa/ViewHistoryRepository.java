package project.airbnb.clone.repository.jpa;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import project.airbnb.clone.entity.ViewHistory;

public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {

    @Modifying
    @Query("UPDATE ViewHistory vh SET vh.viewedAt = CURRENT_TIMESTAMP WHERE vh.accommodation.id = :accommodationId AND vh.guest.id = :guestId")
    int updateViewedAt(@Param("accommodationId") Long accommodationId,
                       @Param("guestId") Long guestId);
}