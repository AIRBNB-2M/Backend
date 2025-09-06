package project.airbnb.clone.service.tour.workers;

import lombok.extern.slf4j.Slf4j;
import project.airbnb.clone.consts.tourapi.IntroAmenity;
import project.airbnb.clone.dto.accommodation.AccommodationProcessorDto;
import project.airbnb.clone.service.tour.TourApiTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
public record DetailIntroWorker(TourApiTemplate tourApiTemplate,
                                AccommodationProcessorDto dto) implements Runnable {

    @Override
    public void run() {
        String contentId = dto.getContentId();

        List<Map<String, String>> items = tourApiTemplate.fetchItems(client -> client.detailIntro(contentId));

        if (items.isEmpty()) {
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
