package project.airbnb.clone.service.tour.workers;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import project.airbnb.clone.common.clients.TourApiClient;
import project.airbnb.clone.consts.DayType;
import project.airbnb.clone.consts.PriceKey;
import project.airbnb.clone.consts.Season;
import project.airbnb.clone.consts.tourapi.InfoAmenity;
import project.airbnb.clone.consts.tourapi.InfoRoomImage;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.dto.TourApiResponse;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
public record DetailInfoWorker(TourApiClient client, AccommodationProcessorDto dto) implements Runnable {

    @Override
    public void run() {
        String contentId = dto.getContentId();
        JsonNode response = client.detailInfo(contentId);

        TourApiResponse apiResponse = new TourApiResponse(response);
        apiResponse.validError();

        List<Map<String, String>> items = apiResponse.getItems();

        if (items.isEmpty()) {
            log.debug("detailInfo.isEmpty, contentId: {}", dto.getContentId());
            return;
        }

        if (items.size() > 10) {
            log.info("detailInfo 10개 이상, contentId: {}", dto.getContentId());
        }

        setAmenities(items);
        setPrice(items);
        setImageUrls(items);
        setMaxPeople(items);
    }

    private void setMaxPeople(List<Map<String, String>> items) {
        Integer max = null;

        for (Map<String, String> item : items) {
            String maxPeople = item.get("roommaxcount").replaceAll("[^0-9]", "").trim();

            if (hasText(maxPeople)) {
                int value = Integer.parseInt(maxPeople);

                if (max == null || max < value) {
                    max = value;
                }
            }
        }

        if (max != null) {
            dto.setMaxPeople(max);
        }
    }

    private void setAmenities(List<Map<String, String>> items) {
        for (Map<String, String> item : items) {
            for (InfoAmenity amenity : InfoAmenity.values()) {
                String amenityName = amenity.getKey();
                dto.putInfoAmenities(amenityName, "Y".equals(item.get(amenityName)));
            }
        }
    }

    private void setPrice(List<Map<String, String>> items) {
        Map<Season, Map<DayType, Integer>> maxPrices = new EnumMap<>(Season.class);
        for (Season season : Season.values()) {
            maxPrices.put(season, new EnumMap<>(DayType.class));
        }

        for (Map<String, String> item : items) {
            for (PriceKey priceKey : PriceKey.values()) {
                int price = Integer.parseInt(item.get(priceKey.getKey()));
                updateMax(maxPrices.get(priceKey.getSeason()), priceKey.getDayType(), price);
            }
        }

        for (Season season : Season.values()) {
            for (DayType dayType : DayType.values()) {
                dto.putPrice(season, dayType, maxPrices.get(season).get(dayType));
            }
        }
    }

    private void updateMax(Map<DayType, Integer> map, DayType dayType, int newPrice) {
        if (newPrice == 0) {
            return;
        }
        map.merge(dayType, newPrice, Integer::max);
    }

    private void setImageUrls(List<Map<String, String>> items) {
        for (Map<String, String> item : items) {
            for (InfoRoomImage roomImage : InfoRoomImage.values()) {
                String url = item.get(roomImage.getKey());

                if (hasText(url)) {
                    dto.addRoomImgUrl(url);
                }
            }
        }
    }
}
