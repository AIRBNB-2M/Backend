package project.airbnb.clone.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import project.airbnb.clone.TestContainerSupport;
import project.airbnb.clone.consts.DayType;
import project.airbnb.clone.consts.Season;
import project.airbnb.clone.repository.redis.RedisRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static project.airbnb.clone.consts.DayType.WEEKDAY;
import static project.airbnb.clone.consts.DayType.WEEKEND;
import static project.airbnb.clone.consts.Season.OFF;
import static project.airbnb.clone.consts.Season.PEAK;

class DateManagerTest extends TestContainerSupport {

    @Autowired DateManager dateManager;
    @Autowired RedisRepository redisRepository;

    @Nested
    @DisplayName("getSeason 메서드 테스트")
    class GetSeasonTest {

        @AfterEach
        void tearDown() {
            redisRepository.deleteValue("holidays:2025");
        }

        @ParameterizedTest(name = "{0}월은 성수기")
        @ValueSource(ints = {2, 7, 8, 12})
        @DisplayName("성공 - 고정 성수기")
        void peakMonths(int month) {
            // given
            LocalDate date = LocalDate.of(2025, month, 1);

            // when
            Season season = dateManager.getSeason(date);

            // then
            assertThat(season).isEqualTo(PEAK);
        }

        @Test
        @DisplayName("성공 - 공휴일은 성수기")
        void holiday_isPeak() {
            // given
            LocalDate date = LocalDate.of(2025, 3, 1); // 삼일절
            String key = "holidays:2025";
            List<String> holidays = List.of("20250301", "20250505", "20250606");

            redisRepository.addSet(key, holidays);

            // when
            Season season = dateManager.getSeason(date);

            // then
            assertThat(season).isEqualTo(PEAK);
        }

        @Test
        @DisplayName("성공 - 평일(비공휴일)은 비성수기")
        void regularDay_isOff() {
            // given
            LocalDate date = LocalDate.of(2025, 3, 10); // 3월 평일
            String key = "holidays:2025";
            List<String> holidays = List.of("20250301", "20250505", "20250606");

            redisRepository.addSet(key, holidays);

            // when
            Season season = dateManager.getSeason(date);

            // then
            assertThat(season).isEqualTo(OFF);
        }

        @Test
        @DisplayName("성공 - Redis에 공휴일 데이터가 없으면 비성수기")
        void noHolidayData_isOff() {
            // given
            LocalDate date = LocalDate.of(2025, 3, 1);
            // Redis에 데이터 없음

            // when
            Season season = dateManager.getSeason(date);

            // then
            assertThat(season).isEqualTo(OFF);
        }
    }

    @Nested
    @DisplayName("getDayType 메서드 테스트")
    class GetDayTypeTest {

        @ParameterizedTest(name = "{0}는 주말")
        @CsvSource({
                "2025-11-08, SATURDAY",
                "2025-11-09, SUNDAY",
                "2025-11-15, SATURDAY",
                "2025-11-16, SUNDAY"
        })
        @DisplayName("성공 - 토요일과 일요일은 주말")
        void weekend(LocalDate date, DayOfWeek dayOfWeek) {
            // when
            DayType dayType = dateManager.getDayType(date);

            // then
            assertThat(dayType).isEqualTo(WEEKEND);
            assertThat(date.getDayOfWeek()).isEqualTo(dayOfWeek);
        }

        @ParameterizedTest(name = "{0}는 평일")
        @CsvSource({
                "2025-11-10, MONDAY",
                "2025-11-11, TUESDAY",
                "2025-11-12, WEDNESDAY",
                "2025-11-13, THURSDAY",
                "2025-11-14, FRIDAY"
        })
        @DisplayName("성공 - 월요일부터 금요일까지는 평일")
        void weekday(LocalDate date, DayOfWeek dayOfWeek) {
            // when
            DayType dayType = dateManager.getDayType(date);

            // then
            assertThat(dayType).isEqualTo(WEEKDAY);
            assertThat(date.getDayOfWeek()).isEqualTo(dayOfWeek);
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationTest {

        @AfterEach
        void tearDown() {
            redisRepository.deleteValue("holidays:2025");
        }

        @ParameterizedTest(name = "{3}")
        @MethodSource("provideSeasonAndDayTypeCombinations")
        @DisplayName("성공 - 성수기/비성수기 & 평일/주말 조합 테스트")
        void seasonAndDayTypeCombinations(LocalDate date,
                                          Season expectedSeason,
                                          DayType expectedDayType,
                                          String description) {
            // given
            if (expectedSeason == OFF) {
                redisRepository.addSet("holidays:2025", List.of("20250301")); // 다른 날짜 등록
            }

            // when
            Season season = dateManager.getSeason(date);
            DayType dayType = dateManager.getDayType(date);

            // then
            assertThat(season).isEqualTo(expectedSeason);
            assertThat(dayType).isEqualTo(expectedDayType);
        }

        static Stream<Arguments> provideSeasonAndDayTypeCombinations() {
            return Stream.of(
                    Arguments.of(LocalDate.of(2025, 8, 2),   // 8월 토요일
                            PEAK, WEEKEND, "성수기 주말"
                    ),
                    Arguments.of(LocalDate.of(2025, 7, 8),   // 7월 화요일
                            PEAK, WEEKDAY, "성수기 평일"
                    ),
                    Arguments.of(LocalDate.of(2025, 4, 5),   // 4월 토요일
                            OFF, WEEKEND, "비성수기 주말"
                    ),
                    Arguments.of(LocalDate.of(2025, 3, 4),   // 3월 화요일
                            OFF, WEEKDAY, "비성수기 평일"
                    )
            );
        }

        @Test
        @DisplayName("성공 - 공휴일(평일인 경우) - 성수기 평일")
        void holidayOnWeekday() {
            // given
            LocalDate date = LocalDate.of(2025, 3, 3); // 월요일이지만 공휴일
            redisRepository.addSet("holidays:2025", List.of("20250303"));

            // when
            Season season = dateManager.getSeason(date);
            DayType dayType = dateManager.getDayType(date);

            // then
            assertThat(season).isEqualTo(PEAK);
            assertThat(dayType).isEqualTo(WEEKDAY); // 요일 자체는 평일
        }
    }
}