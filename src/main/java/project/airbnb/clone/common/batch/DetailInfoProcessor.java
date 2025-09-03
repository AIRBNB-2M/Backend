package project.airbnb.clone.common.batch;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import project.airbnb.clone.common.clients.TourApiClient;
import project.airbnb.clone.consts.tourapi.InfoAmenity;
import project.airbnb.clone.consts.tourapi.InfoRoomImage;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.dto.TourApiResponse;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetailInfoProcessor implements ItemProcessor<AccommodationProcessorDto, AccommodationProcessorDto> {

    private final TourApiClient client;

    @Override
    public AccommodationProcessorDto process(AccommodationProcessorDto dto) {
        String contentId = dto.getContentId();
        JsonNode response = client.detailInfo(contentId);

        TourApiResponse apiResponse = new TourApiResponse(response);

        if (!apiResponse.getError().isEmpty()) {
            log.error("TourAPI 요청 중 오류, {}", apiResponse.getError());
            throw new RuntimeException("Tour API 요청 실패");
        }

        List<Map<String, String>> items = apiResponse.getItems();

        if (items.isEmpty()) {
            log.debug("items.isEmpty == return null, contentId: {}", dto.getContentId());
            return null;
        }

        if (items.size() > 10) {
            log.info("detailInfo 10개 이상, contentId: {}", dto.getContentId());
        }

        setAmenities(dto, items);
        setPrice(dto, items);
        setImageUrls(dto, items);

        return dto;
    }

    private static void setAmenities(AccommodationProcessorDto dto, List<Map<String, String>> items) {
        for (Map<String, String> item : items) {
            for (InfoAmenity amenity : InfoAmenity.values()) {
                String amenityName = amenity.getKey();
                dto.putInfoAmenities(amenityName, "Y".equals(item.get(amenityName)));
            }
        }
    }

    private static void setPrice(AccommodationProcessorDto dto, List<Map<String, String>> items) {
        int minPrice = Integer.MAX_VALUE;
        int maxPrice = Integer.MIN_VALUE;

        for (Map<String, String> item : items) {
            minPrice = Math.min(minPrice, Integer.parseInt(item.get("roomoffseasonminfee1")));
            maxPrice = Math.max(maxPrice, Integer.parseInt(item.get("roompeakseasonminfee2")));
        }

        dto.setMinPrice(minPrice);
        dto.setMaxPrice(maxPrice);
    }

    private static void setImageUrls(AccommodationProcessorDto dto, List<Map<String, String>> items) {
        for (Map<String, String> item : items) {
            for (InfoRoomImage roomImage : InfoRoomImage.values()) {
                String url = item.get(roomImage.getKey());

                if (StringUtils.hasText(url)) {
                    dto.addRoomImgUrl(url);
                }
            }
        }
    }
}
