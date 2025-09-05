package project.airbnb.clone.service.tour.workers;

import lombok.extern.slf4j.Slf4j;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.service.tour.TourApiFacadeManager;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
public record DetailCommonWorker(TourApiFacadeManager tourApiFacadeManager,
                                 AccommodationProcessorDto dto) implements Runnable {

    @Override
    public void run() {
        String contentId = dto.getContentId();

        List<Map<String, String>> items = tourApiFacadeManager.fetchItems(
                client -> client.detailCommon(contentId),
                itemList -> {
                    if (itemList.size() > 1) {
                        log.info("detailCommon 1개 이상, contentId: {}", contentId);
                    }
                });

        if (items.isEmpty()) {
            return;
        }

        Map<String, String> item = items.get(0);

        setIfHasText(item.get("title"), dto::setTitle);                     //제목
        setIfHasText(item.get("tel"), dto::setNumber);                      //전화번호
        setIfHasText(item.get("firstimage"), dto::setThumbnailUrl);         //썸네일이미지

        String areacode = item.get("areacode");
        String sigungucode = item.get("sigungucode");
        if (hasText(areacode) && hasText(sigungucode)) {
            dto.setSigunguCode(areacode + "-" + sigungucode);                               //시군구코드
        }

        setIfHasText(item.get("addr1"), dto::setAddress);                   //주소
        setIfHasText(item.get("overview"), dto::setDescription);            //개요(설명)

        //좌표
        setIfHasText(item.get("mapx"), dto::setMapX, Double::parseDouble);
        setIfHasText(item.get("mapy"), dto::setMapY, Double::parseDouble);
    }

    private void setIfHasText(String value, Consumer<String> setter) {
        if (hasText(value)) {
            setter.accept(value);
        }
    }

    private void setIfHasText(String value, Consumer<Double> setter, Function<String, Double> mapper) {
        if (hasText(value)) {
            setter.accept(mapper.apply(value));
        }
    }
}
