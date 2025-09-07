package project.airbnb.clone.common.events;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import project.airbnb.clone.common.clients.HolidayApiClient;
import project.airbnb.clone.repository.redis.RedisRepository;
import project.airbnb.clone.service.tour.HttpClientTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class HolidaysInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final HttpClientTemplate<HolidayApiClient> clientTemplate;
    private final RedisRepository redisRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
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
}
