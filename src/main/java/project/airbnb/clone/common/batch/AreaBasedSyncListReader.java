package project.airbnb.clone.common.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;
import project.airbnb.clone.common.clients.TourApiClient;
import project.airbnb.clone.dto.accommodation.AccommodationProcessorDto;
import project.airbnb.clone.repository.facade.TourRepositoryFacadeManager;
import project.airbnb.clone.service.tour.HttpClientTemplate;
import project.airbnb.clone.service.tour.workers.AreaListWorker;

import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AreaBasedSyncListReader implements ItemReader<AccommodationProcessorDto> {

    private final HttpClientTemplate<TourApiClient> httpClientTemplate;
    private final TourRepositoryFacadeManager tourRepositoryFacadeManager;

    private int pageNo = 1;
    private Iterator<AccommodationProcessorDto> currentIter;

    //TODO : 배치에서 일일 트래픽(1,000건)을 초과했을 때 적절한 처리 필요
    @Override
    public AccommodationProcessorDto read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        if (currentIter == null || !currentIter.hasNext()) {
            int numOfRows = 100;
            log.debug("남아있는 데이터가 없어 새로 요청");

            AreaListWorker worker = new AreaListWorker(httpClientTemplate, tourRepositoryFacadeManager);
            List<AccommodationProcessorDto> dtos = worker.run(pageNo, numOfRows);

            currentIter = dtos.iterator();
            pageNo++;
        }

        return currentIter.hasNext() ? currentIter.next() : null;
    }
}
