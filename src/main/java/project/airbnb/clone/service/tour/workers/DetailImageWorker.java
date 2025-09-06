package project.airbnb.clone.service.tour.workers;

import lombok.extern.slf4j.Slf4j;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.service.tour.TourApiTemplate;

import java.util.List;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
public record DetailImageWorker(TourApiTemplate tourApiTemplate,
                                AccommodationProcessorDto dto) implements Runnable {

    @Override
    public void run() {
        String contentId = dto.getContentId();

        List<Map<String, String>> items = tourApiTemplate.fetchItems(
                client -> client.detailImage(contentId),
                itemList -> {
                    if (itemList.size() > 10) {
                        log.info("detailImage 10개 이상, contentId: {}", dto.getContentId());
                    }
                });

        if (items.isEmpty()) {
            return;
        }

        for (Map<String, String> item : items) {
            String url = item.get("originimgurl");

            if (hasText(url)) {
                dto.addOriginImgUrl(url);
            }
        }
    }
}
