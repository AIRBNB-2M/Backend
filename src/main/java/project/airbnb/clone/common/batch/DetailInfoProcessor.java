package project.airbnb.clone.common.batch;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import project.airbnb.clone.common.clients.TourApiClient;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.dto.TourApiResponse;

import java.util.ArrayList;
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
        JsonNode response = client.detailInfo(contentId, 0, 0);

        TourApiResponse apiResponse = new TourApiResponse(response);
        List<Map<String, String>> items = apiResponse.getItems();

        if (items.isEmpty()) {
            log.debug("items.isEmpty == return null");
            return null;
        }

        int sum = 0;
        int count = 0;

        List<String> priceKeys = List.of("roomoffseasonminfee1", "roomoffseasonminfee2", "roompeakseasonminfee1", "roompeakseasonminfee2");
        List<String> amenityKeys = List.of(
                "roombathfacility",
                "roombath",
                "roomhometheater",
                "roomaircondition",
                "roomtv",
                "roompc",
                "roomcable",
                "roominternet",
                "roomrefrigerator",
                "roomtoiletries",
                "roomsofa",
                "roomcook",
                "roomtable",
                "roomhairdryer"
        );
        List<String> imageKeys = List.of("roomimg1", "roomimg2", "roomimg3", "roomimg4", "roomimg5");

        for (Map<String, String> item : items) {
            for (String key : priceKeys) {
                sum += Integer.parseInt(item.get(key));
                count++;
            }
        }

        int price = (int) Math.ceil((double) sum / count);
        dto.setPrice(price);

        List<String> imageUrls = new ArrayList<>();
        for (Map<String, String> item : items) {
            for (String key : imageKeys) {
                imageUrls.add(item.get(key));
            }
        }

        dto.setRoomImgUrls(imageUrls);

        return dto;
    }
}
