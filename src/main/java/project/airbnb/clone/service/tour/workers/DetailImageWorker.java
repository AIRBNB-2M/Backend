package project.airbnb.clone.service.tour.workers;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import project.airbnb.clone.common.clients.TourApiClient;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.dto.TourApiResponse;

import java.util.List;
import java.util.Map;

@Slf4j
public record DetailImageWorker(TourApiClient client, AccommodationProcessorDto dto) implements Runnable {

    @Override
    public void run() {
        String contentId = dto.getContentId();
        JsonNode response = client.detailImage(contentId);

        TourApiResponse apiResponse = new TourApiResponse(response);
        apiResponse.validError();

        List<Map<String, String>> items = apiResponse.getItems();

        if (items.isEmpty()) {
            log.debug("detailImage.isEmpty, contentId: {}", dto.getContentId());
            return;
        }

        if (items.size() > 10) {
            log.info("detailImage 10개 이상, contentId: {}", dto.getContentId());
        }

        for (Map<String, String> item : items) {
            String url = item.get("originimgurl");

            if (StringUtils.hasText(url)) {
                dto.addOriginImgUrl(url);
            }
        }
    }
}
