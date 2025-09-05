package project.airbnb.clone.service.tour.workers;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import project.airbnb.clone.common.clients.TourApiClient;
import project.airbnb.clone.consts.tourapi.IntroAmenity;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.dto.TourApiResponse;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
public record DetailIntroWorker(TourApiClient client, AccommodationProcessorDto dto) implements Runnable {

    @Override
    public void run() {
        String contentId = dto.getContentId();
        JsonNode response = client.detailIntro(contentId);

        TourApiResponse apiResponse = new TourApiResponse(response);
        apiResponse.validError();

        List<Map<String, String>> items = apiResponse.getItems();

        if (items.isEmpty()) {
            log.debug("detailIntro.isEmpty, contentId: {}", dto.getContentId());
            return;
        }

        Map<String, String> item = items.get(0);

        setIfHasText(item.get("checkintime"), dto::setCheckIn);                     //체크인
        setIfHasText(item.get("checkouttime"), dto::setCheckOut);                   //체크아웃
        setIfHasText(item.get("refundregulation"), dto::setRefundRegulation);       //환불규정

        for (IntroAmenity amenity : IntroAmenity.values()) {
            String amenityName = amenity.getKey();
            dto.putIntroAmenities(amenityName, "1".equals(item.get(amenityName)));
        }
    }

    private void setIfHasText(String value, Consumer<String> setter) {
        if (hasText(value)) {
            setter.accept(value);
        }
    }
}
