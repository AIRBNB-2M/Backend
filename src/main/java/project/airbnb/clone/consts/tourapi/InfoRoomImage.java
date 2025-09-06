package project.airbnb.clone.consts.tourapi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InfoRoomImage {
    ROOM_IMAGE_1("roomimg1"),
    ROOM_IMAGE_2("roomimg2"),
    ROOM_IMAGE_3("roomimg3"),
    ROOM_IMAGE_4("roomimg4"),
    ROOM_IMAGE_5("roomimg5");

    private final String key;
}
