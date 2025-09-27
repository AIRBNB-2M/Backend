package project.airbnb.clone.service.tour;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import project.airbnb.clone.common.clients.HolidayApiClient;
import project.airbnb.clone.repository.redis.RedisRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HttpClientTemplate<HolidayApiClient> clientTemplate;
    private final RedisRepository redisRepository;

    @Retryable(retryFor = {RuntimeException.class}, maxAttempts = 2, backoff = @Backoff(delay = 2000))
    public void initHolidays() {
        int year = LocalDate.now().getYear();
        String key = "holidays:" + year;

        if (redisRepository.hasKey(key)) {
            return;
        }

        List<Map<String, String>> items = clientTemplate.fetchItems(client -> client.getHolidays(year));
        List<String> holidays = items.stream()
                                     .map(map -> map.get("locdate"))
                                     .toList();
        redisRepository.addSet(key, holidays);
    }

    @Recover
    public void recover(RuntimeException e) {
        log.error("공공데이터(공휴일) API 초기화 실패: ", e);
    }
}