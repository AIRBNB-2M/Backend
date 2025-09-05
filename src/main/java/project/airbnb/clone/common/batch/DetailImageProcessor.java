package project.airbnb.clone.common.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.service.tour.TourApiFacadeManager;
import project.airbnb.clone.service.tour.workers.DetailImageWorker;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetailImageProcessor implements ItemProcessor<AccommodationProcessorDto, AccommodationProcessorDto> {

    private final TourApiFacadeManager tourApiFacadeManager;

    @Override
    public AccommodationProcessorDto process(AccommodationProcessorDto dto) {
        DetailImageWorker worker = new DetailImageWorker(tourApiFacadeManager, dto);
        worker.run();

        return dto.hasThumbnail() ? dto : null;
    }
}
