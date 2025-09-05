package project.airbnb.clone.service.tour.workers;

import lombok.extern.slf4j.Slf4j;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.service.tour.TourApiFacadeManager;
import project.airbnb.clone.service.tour.TourRepositoryFacadeManager;

import java.util.List;
import java.util.Map;

@Slf4j
public record AreaListWorker(TourApiFacadeManager tourApiFacadeManager, TourRepositoryFacadeManager tourRepositoryFacadeManager) {

    public List<AccommodationProcessorDto> run(int pageNo, int numOfRows) {
        List<Map<String, String>> items = tourApiFacadeManager.fetchItems(client -> client.getAreaList(pageNo, numOfRows));

        List<String> contentIds = items.stream()
                                       .map(item -> item.get("contentid"))
                                       .toList();

        Map<String, Accommodation> existings = tourRepositoryFacadeManager.findByContentIdInToMap(contentIds);

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
