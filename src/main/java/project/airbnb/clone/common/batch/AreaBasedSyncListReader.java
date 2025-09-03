package project.airbnb.clone.common.batch;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;
import project.airbnb.clone.common.clients.TourApiClient;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.dto.TourApiResponse;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.repository.AccommodationRepository;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AreaBasedSyncListReader implements ItemReader<AccommodationProcessorDto> {

    private final TourApiClient client;
    private final AccommodationRepository repository;

    private int pageNo = 1;
    private Iterator<AccommodationProcessorDto> currentIter;

    @Override
    public AccommodationProcessorDto read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        if (currentIter == null || !currentIter.hasNext()) {
            log.debug("남아있는 데이터가 없어 새로 요청");
            int numOfRows = 100;

            JsonNode response = client.getAreaList(pageNo, numOfRows);
            TourApiResponse apiResponse = new TourApiResponse(response);

            if (!apiResponse.getError().isEmpty()) {
                log.error("TourAPI 요청 중 오류, {}", apiResponse.getError());
                return null;
            }

            List<Map<String, String>> items = apiResponse.getItems();
            if (items.isEmpty()) {
                log.debug("items.isEmpty == return null");
                return null;
            }

            List<String> contentIds = items.stream()
                                           .map(item -> item.get("contentid"))
                                           .toList();

            Map<String, Accommodation> exisitings = repository.findByContentIdIn(contentIds)
                                                              .stream()
                                                              .collect(Collectors.toMap(Accommodation::getContentId, Function.identity()));

            List<AccommodationProcessorDto> dtos = items.stream()
                                                        .map(item ->
                                                                new AccommodationProcessorDto(
                                                                        item.get("contentid"),
                                                                        item.get("modifiedtime")
                                                                ))
                                                        .filter(dto -> {
                                                            Accommodation existing = exisitings.get(dto.getContentId());
                                                            if (existing == null) {
                                                                dto.setNew(true);
                                                                return true;
                                                            }
                                                            return dto.getModifiedTime().isAfter(existing.getModifiedTime());
                                                        })
                                                        .toList();
            currentIter = dtos.iterator();
            pageNo++;
        }

        if (!currentIter.hasNext()) {
            log.debug("currentIter.notHasNext == return null");
        }

        return currentIter.hasNext() ? currentIter.next() : null;
    }
}
