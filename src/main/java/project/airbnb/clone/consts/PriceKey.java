package project.airbnb.clone.consts;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static project.airbnb.clone.consts.DayType.*;
import static project.airbnb.clone.consts.Season.*;

@Getter
@AllArgsConstructor
public enum PriceKey {
    OFF_WEEKDAY("roomoffseasonminfee1", OFF, WEEKDAY),
    OFF_WEEKEND("roomoffseasonminfee2", OFF, WEEKEND),
    PEAK_WEEKDAY("roompeakseasonminfee1", PEAK, WEEKDAY),
    PEAK_WEEKEND("roompeakseasonminfee2", PEAK, WEEKEND);

    private final String key;
    private final Season season;
    private final DayType dayType;
}
