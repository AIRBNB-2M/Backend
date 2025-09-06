package project.airbnb.clone.dto.accommodation;

import lombok.Data;
import project.airbnb.clone.common.batch.DetailCommonProcessor;
import project.airbnb.clone.common.batch.DetailImageProcessor;
import project.airbnb.clone.common.batch.DetailInfoProcessor;
import project.airbnb.clone.common.batch.DetailIntroProcessor;
import project.airbnb.clone.consts.DayType;
import project.airbnb.clone.consts.Season;
import project.airbnb.clone.service.tour.workers.DetailCommonWorker;
import project.airbnb.clone.service.tour.workers.DetailImageWorker;
import project.airbnb.clone.service.tour.workers.DetailInfoWorker;
import project.airbnb.clone.service.tour.workers.DetailIntroWorker;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

@Data
public class AccommodationProcessorDto {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private boolean isNew = false;

    // areaBasedSyncList2
    private final String contentId;
    private final LocalDateTime modifiedTime;

    public AccommodationProcessorDto(String contentId, String modifiedTime) {
        this.contentId = contentId;
        this.modifiedTime = LocalDateTime.parse(modifiedTime, FORMATTER);
        initPricesMap();
    }

    private void initPricesMap() {
        this.prices = new EnumMap<>(Season.class);
        for (Season season : Season.values()) {
            prices.put(season, new EnumMap<>(DayType.class));

            for (DayType dayType : DayType.values()) {
                prices.get(season).put(dayType, null);
            }
        }
    }

    /**
     * /detailCommon2
     * @see DetailCommonWorker
     * @see DetailCommonProcessor
     */
    private String number;
    private String title;
    private String thumbnailUrl;
    private String sigunguCode;
    private String address;
    private String description;
    private Double mapX;
    private Double mapY;

    /**
     * /detailIntro2
     * @see DetailIntroWorker
     * @see DetailIntroProcessor
     */
    private String checkIn;
    private String checkOut;
    private String refundRegulation;
    private Map<String, Boolean> introAmenities = new HashMap<>();

    public void putIntroAmenities(String amenity, boolean available) {
        this.introAmenities.put(amenity, available);
    }

    /**
     * /detailInfo2
     * @see DetailInfoWorker
     * @see DetailInfoProcessor
     */
    private Integer maxPeople;
    private Map<Season, Map<DayType, Integer>> prices;
    private Map<String, Boolean> infoAmenities = new HashMap<>();
    private List<String> roomImgUrls = new ArrayList<>();

    public void putInfoAmenities(String amenity, boolean available) {
        this.infoAmenities.put(amenity, available);
    }

    public void addRoomImgUrl(String url) {
        this.roomImgUrls.add(url);
    }

    public void putPrice(Season season, DayType dayType, Integer price) {
        this.prices.get(season).put(dayType, price);
    }

    public Integer getPrice(Season season, DayType dayType) {
        return this.prices.get(season).get(dayType);
    }

    public boolean hasAllPrices() {
        for (Season season : Season.values()) {
            for (DayType dayType : DayType.values()) {
                if (getPrice(season, dayType) == null) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * /detailImage2
     * @see DetailImageWorker
     * @see DetailImageProcessor
     */
    private List<String> originImgUrls = new ArrayList<>();

    public void addOriginImgUrl(String url) {
        this.originImgUrls.add(url);
    }

    public boolean hasThumbnail() {
        return hasText(thumbnailUrl) || !roomImgUrls.isEmpty() || !originImgUrls.isEmpty();
    }
}
