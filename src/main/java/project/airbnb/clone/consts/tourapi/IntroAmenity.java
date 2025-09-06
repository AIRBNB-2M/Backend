package project.airbnb.clone.consts.tourapi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IntroAmenity {
    BARBECUE("barbecue"),
    BEAUTY("beauty"),
    BEVERAGE("beverage"),
    BICYCLE("bicycle"),
    CAMPFIRE("campfire"),
    FITNESS("fitness"),
    KARAOKE("karaoke"),
    PUBLIC_BATH("publicbath"),
    PUBLIC_PC("publicpc"),
    SAUNA("sauna"),
    SEMINAR("seminar"),
    SPORTS("sports");

    private final String key;
}
