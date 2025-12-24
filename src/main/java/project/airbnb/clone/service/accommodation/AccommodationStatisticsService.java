package project.airbnb.clone.service.accommodation;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccommodationStatisticsService {

    private final EntityManager em;

    @Scheduled(cron = "* * 2 * * *")
    public void refreshStats() {
        log.info("지역별 인기 숙소 TOP N 통계 갱신");

        em.createNativeQuery("DELETE FROM accommodation_stats").executeUpdate();
        em.flush();

        String sql = """
                INSERT INTO accommodation_stats (accommodation_id, area_code, area_name, title, average_rating, reservation_count, thumbnail_url)
                SELECT
                    ranked.accommodation_id,
                    ac.area_code,
                    ac.code_name,
                    ranked.title,
                    ranked.average_rating,
                    ranked.reservation_count,
                    ai.image_url
                FROM (
                    SELECT
                        a.accommodation_id,
                        a.title,
                        a.average_rating,
                        a.reservation_count,
                        sc.area_code,
                        ROW_NUMBER() OVER (
                            PARTITION BY sc.area_code
                            ORDER BY a.reservation_count DESC, a.average_rating DESC
                        ) AS rn
                    FROM accommodations a
                    JOIN sigungu_codes sc ON sc.sigungu_code = a.sigungu_code
                ) ranked
                JOIN area_codes ac ON ac.area_code = ranked.area_code
                JOIN accommodation_images ai
                ON ai.accommodation_id = ranked.accommodation_id
                AND ai.thumbnail = true
                WHERE ranked.rn <= 8
                """;

        em.createNativeQuery(sql).executeUpdate();
        em.flush();
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void refreshRecentStats() {
        log.info("숙소 반정규화 통계 필드 갱신 - 최근 변경");
        String sql = """
                UPDATE accommodations a
                SET
                    a.reservation_count = (
                        SELECT COUNT(*)
                        FROM reservations r
                        WHERE r.accommodation_id = a.accommodation_id
                          AND r.status != 'CANCELED'
                    ),
                    a.average_rating = COALESCE((
                        SELECT ROUND(AVG(rv.rating), 2)
                        FROM reviews rv
                        JOIN reservations rs ON rv.reservation_id = rs.reservation_id
                        WHERE rs.accommodation_id = a.accommodation_id
                    ), 0.0)
                WHERE a.accommodation_id IN (
                    SELECT DISTINCT accommodation_id
                    FROM reservations
                    WHERE updated_at >= DATE_SUB(NOW(), INTERVAL 30 MINUTE)
                    UNION
                    SELECT DISTINCT rs.accommodation_id
                    FROM reviews rv
                    JOIN reservations rs ON rv.reservation_id = rs.reservation_id
                    WHERE rv.updated_at >= DATE_SUB(NOW(), INTERVAL 30 MINUTE)
                )
                """;
        em.createNativeQuery(sql)
          .executeUpdate();
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void refreshAllStats() {
        log.info("숙소 반정규화 통계 필드 갱신 - 전체");
        String sql = """
                UPDATE accommodations a
                SET
                    a.reservation_count = COALESCE((
                        SELECT COUNT(*)
                        FROM reservations r
                        WHERE r.accommodation_id = a.accommodation_id
                          AND r.status != 'CANCELED'
                    ), 0),
                    a.average_rating = COALESCE((
                        SELECT ROUND(AVG(rv.rating), 2)
                        FROM reviews rv
                        JOIN reservations rs ON rv.reservation_id = rs.reservation_id
                        WHERE rs.accommodation_id = a.accommodation_id
                    ), 0.0)
                """;
        em.createNativeQuery(sql)
          .executeUpdate();
    }
}
