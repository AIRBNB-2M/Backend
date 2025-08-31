package project.airbnb.clone.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Data
public class AccommodationProcessorDto {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

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
    private String areaCode;
    private String sigunguCode;
    private String address;
    private String description;
    private Double mapX;
    private Double mapY;

    //detailIntro2
    private String checkIn;
    private String checkOut;
    private Map<String, Boolean> introAmenities;

    //detailInfo2
    private Integer maxPeople;
    private Integer price;
    private List<String> roomImgUrls;

    //detailImage2
    private List<String> originImgUrls;

    private LocalDateTime parseTime(String modifiedTime) {
        return LocalDateTime.parse(modifiedTime, FORMATTER);
    }
}
