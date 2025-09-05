package project.airbnb.clone.common.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.service.tour.TourApiFacadeManager;
import project.airbnb.clone.service.tour.workers.DetailCommonWorker;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetailCommonProcessor implements ItemProcessor<AccommodationProcessorDto, AccommodationProcessorDto> {

    private final TourApiFacadeManager tourApiFacadeManager;

    @Override
    public AccommodationProcessorDto process(AccommodationProcessorDto dto) {
        DetailCommonWorker worker = new DetailCommonWorker(tourApiFacadeManager, dto);
        worker.run();

        return hasMandatoryFields(dto) ? dto : null;
    }

    private boolean hasMandatoryFields(AccommodationProcessorDto dto) {
        return hasText(dto.getTitle()) && hasText(dto.getSigunguCode()) &&
                hasText(dto.getAddress()) && dto.getMapX() != null && dto.getMapY() != null &&
                hasText(dto.getContentId()) && dto.getModifiedTime() != null;
    }
}
