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
        JsonNode response = client.detailCommon(contentId);

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

        if (items.size() > 1) {
            log.info("detailCommon 1개 이상, contentId: {}", dto.getContentId());
        }
        Map<String, String> item = items.get(0);

        dto.setTitle(item.get("title"));                                            //제목
        dto.setNumber(item.get("tel"));                                             //전화번호
        dto.setThumbnailUrl(item.get("firstimage"));                                //썸네일이미지
        dto.setSigunguCode(item.get("areacode") + "-" + item.get("sigungucode"));   //시군구코드
        dto.setAddress(item.get("addr1"));                                          //주소
        dto.setMapX(Double.parseDouble(item.get("mapx")));                      //좌표(X)
        dto.setMapY(Double.parseDouble(item.get("mapy")));                      //좌표(Y)
        dto.setDescription(item.get("overview"));                                   //개요(설명)

        return dto;
    }
}
