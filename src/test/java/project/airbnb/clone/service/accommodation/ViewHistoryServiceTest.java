package project.airbnb.clone.service.accommodation;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import project.airbnb.clone.TestContainerSupport;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.entity.AreaCode;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.entity.SigunguCode;
import project.airbnb.clone.entity.ViewHistory;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ViewHistoryServiceTest extends TestContainerSupport {

    @Autowired ViewHistoryService viewHistoryService;
    @Autowired EntityManager em;

    Guest guest;
    Accommodation accommodation;

    @BeforeEach
    void setUp() {
        // Guest 생성
        guest = Guest.createForTest();
        em.persist(guest);

        // 지역 코드 생성
        AreaCode areaCode = AreaCode.create("11", "서울");
        em.persist(areaCode);

        SigunguCode sigunguCode = SigunguCode.create("11680", "강남구", areaCode);
        em.persist(sigunguCode);

        // Accommodation 생성
        accommodation = Accommodation.forTest("테스트 숙소", sigunguCode, 1.0, 1.1);
        em.persist(accommodation);
    }

    @Nested
    @DisplayName("saveRecentView 메서드 테스트")
    class SaveRecentViewTest {

        @Test
        @DisplayName("성공 - 최초 조회 시 새로운 ViewHistory 생성")
        void saveRecentView_create_new() {
            // given
            Long accommodationId = accommodation.getId();
            Long guestId = guest.getId();

            // when
            viewHistoryService.saveRecentView(accommodationId, guestId);
            em.flush();
            em.clear();

            // then
            ViewHistory result = findViewHistory(accommodationId, guestId);

            assertThat(result).isNotNull();
            assertThat(result.getAccommodation().getId()).isEqualTo(accommodationId);
            assertThat(result.getGuest().getId()).isEqualTo(guestId);
            assertThat(result.getViewedAt()).isNotNull();
            assertThat(result.getViewedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("성공 - 기존 조회 이력이 있으면 viewedAt만 업데이트")
        void saveRecentView_update_existing() {
            // given
            Long accommodationId = accommodation.getId();
            Long guestId = guest.getId();

            LocalDateTime firstViewTime = LocalDateTime.now().minusHours(2);
            ViewHistory existingHistory = ViewHistory.create(guest, accommodation, firstViewTime);
            em.persist(existingHistory);
            Long existingId = existingHistory.getId();

            em.flush();
            em.clear();

            // when
            viewHistoryService.saveRecentView(accommodationId, guestId);
            em.flush();
            em.clear();

            // then
            ViewHistory result = findViewHistory(accommodationId, guestId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(existingId); // 같은 엔티티
            assertThat(result.getViewedAt()).isAfter(firstViewTime); // 시간만 업데이트됨
            assertThat(result.getViewedAt()).isBeforeOrEqualTo(LocalDateTime.now());

            // 새로운 레코드가 생성되지 않았는지 확인
            long count = countViewHistories(accommodationId, guestId);
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("성공 - 동일 숙소를 여러 번 조회해도 1개의 이력만 유지")
        void saveRecentView_multiple_times() {
            // given
            Long accommodationId = accommodation.getId();
            Long guestId = guest.getId();

            // when - 3번 조회
            viewHistoryService.saveRecentView(accommodationId, guestId);
            viewHistoryService.saveRecentView(accommodationId, guestId);
            viewHistoryService.saveRecentView(accommodationId, guestId);

            em.flush();
            em.clear();

            // then
            long count = countViewHistories(accommodationId, guestId);

            assertThat(count).isEqualTo(1);
        }
    }

    private ViewHistory findViewHistory(Long accommodationId, Long guestId) {
        List<ViewHistory> results = em.createQuery("SELECT vh FROM ViewHistory vh " +
                                                      "WHERE vh.accommodation.id = :accommodationId " +
                                                      "AND vh.guest.id = :guestId", ViewHistory.class)
                                      .setParameter("accommodationId", accommodationId)
                                      .setParameter("guestId", guestId)
                                      .getResultList();

        return results.isEmpty() ? null : results.get(0);
    }

    private long countViewHistories(Long accommodationId, Long guestId) {
        return em.createQuery("SELECT COUNT(vh) FROM ViewHistory vh " +
                                 "WHERE vh.accommodation.id = :accommodationId " +
                                 "AND vh.guest.id = :guestId", Long.class)
                 .setParameter("accommodationId", accommodationId)
                 .setParameter("guestId", guestId)
                 .getSingleResult();
    }
}