package project.airbnb.clone.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import project.airbnb.clone.consts.DayType;
import project.airbnb.clone.consts.Season;
import project.airbnb.clone.repository.redis.RedisRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static project.airbnb.clone.consts.DayType.WEEKDAY;
import static project.airbnb.clone.consts.DayType.WEEKEND;
import static project.airbnb.clone.consts.Season.OFF;
import static project.airbnb.clone.consts.Season.PEAK;

@Component
@RequiredArgsConstructor
public class DateManager {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RedisRepository redisRepository;

    public Season getSeason(LocalDate date) {
        int month = date.getMonthValue();

        if (month == 2 || month == 7 || month == 8 || month == 12) {
            return PEAK;
        }

        String key = "holidays:" + date.getYear();
        String value = date.format(DATE_FORMATTER);

        return redisRepository.isMemberOfSet(key, value) ? PEAK : OFF;
    }

    public DayType getDayType(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        if (dayOfWeek == SATURDAY || dayOfWeek == SUNDAY) {
            return WEEKEND;
        }
        return WEEKDAY;
    }
}
