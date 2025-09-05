package project.airbnb.clone.common.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.service.tour.TourApiFacadeManager;
import project.airbnb.clone.service.tour.workers.DetailIntroWorker;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetailIntroProcessor implements ItemProcessor<AccommodationProcessorDto, AccommodationProcessorDto> {

    private final TourApiFacadeManager tourApiFacadeManager;

    @Override
    public AccommodationProcessorDto process(AccommodationProcessorDto dto) {
        DetailIntroWorker worker = new DetailIntroWorker(tourApiFacadeManager, dto);
        worker.run();

        return dto;
    }
}
