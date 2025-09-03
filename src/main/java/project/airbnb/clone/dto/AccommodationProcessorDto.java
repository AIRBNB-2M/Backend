package project.airbnb.clone.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class AccommodationProcessorDto {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private boolean isNew = false;

    // areaBasedSyncList2
    private final String contentId;
    private final LocalDateTime modifiedTime;

    public AccommodationProcessorDto(String contentId, String modifiedTime) {
        this.contentId = contentId;
        this.modifiedTime = parseTime(modifiedTime);
    }

    //detailCommon2
    private String number;
    private String title;
    private String thumbnailUrl;
    private String sigunguCode;
    private String address;
    private String description;
    private Double mapX;
    private Double mapY;

    //detailIntro2
    private String checkIn;
    private String checkOut;
    private Map<String, Boolean> introAmenities = new HashMap<>();

    //detailInfo2
    private Integer maxPeople;
    private Integer minPrice;
    private Integer maxPrice;
    private Map<String, Boolean> infoAmenities = new HashMap<>();
    private List<String> roomImgUrls = new ArrayList<>();

    //detailImage2
    private List<String> originImgUrls = new ArrayList<>();

    private LocalDateTime parseTime(String modifiedTime) {
        return LocalDateTime.parse(modifiedTime, FORMATTER);
    }

    public void putIntroAmenities(String amenity, boolean available) {
        this.introAmenities.put(amenity, available);
    }

    public void putInfoAmenities(String amenity, boolean available) {
        this.infoAmenities.put(amenity, available);
    }

    public void addRoomImgUrl(String url) {
        this.roomImgUrls.add(url);
    }

    public void addOriginImgUrl(String url) {
        this.originImgUrls.add(url);
    }
}
