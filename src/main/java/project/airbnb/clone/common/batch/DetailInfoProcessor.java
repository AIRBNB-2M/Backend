package project.airbnb.clone.common.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import project.airbnb.clone.common.clients.TourApiClient;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.service.tour.workers.DetailInfoWorker;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetailInfoProcessor implements ItemProcessor<AccommodationProcessorDto, AccommodationProcessorDto> {

    private final TourApiClient client;

    @Override
    public AccommodationProcessorDto process(AccommodationProcessorDto dto) {
        DetailInfoWorker worker = new DetailInfoWorker(client, dto);
        worker.run();

        return dto.hasThumbnail() ? dto : null;
    }
}
