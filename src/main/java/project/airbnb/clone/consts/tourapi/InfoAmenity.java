package project.airbnb.clone.consts.tourapi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InfoAmenity {
    ROOM_BATH_FACILITY("roombathfacility"),
    ROOM_BATH("roombath"),
    ROOM_HOME_THEATER("roomhometheater"),
    ROOM_AIR_CONDITION("roomaircondition"),
    ROOM_TV("roomtv"),
    ROOM_PC("roompc"),
    ROOM_CABLE("roomcable"),
    ROOM_INTERNET("roominternet"),
    ROOM_REFRIGERATOR("roomrefrigerator"),
    ROOM_TOILETRIES("roomtoiletries"),
    ROOM_SOFA("roomsofa"),
    ROOM_COOK("roomcook"),
    ROOM_TABLE("roomtable"),
    ROOM_HAIRDRYER("roomhairdryer");

    private final String key;
}
