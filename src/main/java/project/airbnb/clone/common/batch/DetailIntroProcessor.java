package project.airbnb.clone.common.batch;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import project.airbnb.clone.common.clients.TourApiClient;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.dto.TourApiResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetailIntroProcessor implements ItemProcessor<AccommodationProcessorDto, AccommodationProcessorDto> {

    private final TourApiClient client;

    @Override
    public AccommodationProcessorDto process(AccommodationProcessorDto dto) {
        String contentId = dto.getContentId();
        JsonNode response = client.detailIntro(contentId, 0, 0);

        TourApiResponse apiResponse = new TourApiResponse(response);
        List<Map<String, String>> items = apiResponse.getItems();

        if (items.isEmpty()) {
            log.debug("items.isEmpty == return null");
            return null;
        }

        Pattern pattern = Pattern.compile("(\\d{1,2}:\\d{2})");

        Map<String, String> item = items.get(0);

        Matcher matcher = pattern.matcher(item.get("checkintime"));
        if (!matcher.matches()) {
            log.debug("!checkintime.matches == return null");
            return null;
        }
        if (matcher.find()) {
            String text = matcher.group(1);
            dto.setCheckIn(text);
        }

        matcher = pattern.matcher(item.get("checkouttime"));
        if (!matcher.matches()) {
            log.debug("!checkouttime.matches == return null");
            return null;
        }
        if (matcher.find()) {
            dto.setCheckOut("");
        }

        List<String> amenities = List.of("chkcooking", "barbecue", "beauty", "beverage", "bicycle", "campfire", "fitness", "karaoke","publicbath", "publicpc", "sauna", "seminar", "sports");
        Map<String, Boolean> temp = new HashMap<>();
        for (String amenity : amenities) {
            temp.put(amenity, item.get(amenity).equals("1"));
        }

        dto.setIntroAmenities(temp);

        return dto;
    }
}
