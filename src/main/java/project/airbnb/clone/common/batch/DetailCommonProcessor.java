package project.airbnb.clone.common.batch;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import project.airbnb.clone.common.clients.TourApiClient;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.dto.TourApiResponse;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetailCommonProcessor implements ItemProcessor<AccommodationProcessorDto, AccommodationProcessorDto> {

    private final TourApiClient client;

    @Override
    public AccommodationProcessorDto process(AccommodationProcessorDto dto) {
        String contentId = dto.getContentId();
        JsonNode response = client.detailCommon(contentId, 0, 0);

        TourApiResponse apiResponse = new TourApiResponse(response);
        List<Map<String, String>> items = apiResponse.getItems();

        if (items.isEmpty()) {
            log.debug("items.isEmpty == return null");
            return null;
        }

        Map<String, String> item = items.get(0);

        dto.setTitle(item.get("title"));
        dto.setNumber(item.get("tel"));
        dto.setThumbnailUrl(item.get("firstimage"));
        dto.setAreaCode(item.get("areacode"));
        dto.setSigunguCode(item.get("sigungucode"));
        dto.setAddress(item.get("addr1"));
        dto.setMapX(Double.parseDouble(item.get("mapx")));
        dto.setMapY(Double.parseDouble(item.get("mapy")));
        dto.setDescription(item.get("overview"));

        return dto;
    }
}
