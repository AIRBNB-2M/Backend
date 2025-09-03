package project.airbnb.clone.common.batch;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import project.airbnb.clone.common.clients.TourApiClient;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.dto.TourApiResponse;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetailImageProcessor implements ItemProcessor<AccommodationProcessorDto, AccommodationProcessorDto> {

    private final TourApiClient client;

    @Override
    public AccommodationProcessorDto process(AccommodationProcessorDto dto) {
        String contentId = dto.getContentId();
        JsonNode response = client.detailImage(contentId);

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
            log.info("detailImage 10개 이상, contentId: {}", dto.getContentId());
        }

        for (Map<String, String> item : items) {
            String url = item.get("originimgurl");

            if (StringUtils.hasText(url)) {
                dto.addOriginImgUrl(url);
            }
        }

        return dto;
    }
}
