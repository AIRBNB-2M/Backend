package project.airbnb.clone.service.tour.workers;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import project.airbnb.clone.common.clients.TourApiClient;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.dto.TourApiResponse;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.repository.AccommodationRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public record AreaListWorker(TourApiClient client, AccommodationRepository accommodationRepository) {

    public List<AccommodationProcessorDto> run(int pageNo, int numOfRows) {
        JsonNode areaList = client.getAreaList(pageNo, numOfRows);

        TourApiResponse apiResponse = new TourApiResponse(areaList);
        apiResponse.validError();

        List<Map<String, String>> items = apiResponse.getItems();
        if (items.isEmpty()) {
            log.debug("getAreaList items.isEmpty");
            return Collections.emptyList();
        }

        List<String> contentIds = items.stream()
                                       .map(item -> item.get("contentid"))
                                       .toList();

        Map<String, Accommodation> existings = accommodationRepository.findByContentIdIn(contentIds)
                                                                      .stream()
                                                                      .collect(Collectors.toMap(Accommodation::getContentId, Function.identity()));
        return items.stream()
                    .map(item ->
                            new AccommodationProcessorDto(
                                    item.get("contentid"),
                                    item.get("modifiedtime")
                            ))
                    .filter(dto -> {
                        Accommodation existing = existings.get(dto.getContentId());
                        if (existing == null) {
                            return true;
                        }
                        return dto.getModifiedTime().isAfter(existing.getModifiedTime());
                    })
                    .toList();
    }
}
