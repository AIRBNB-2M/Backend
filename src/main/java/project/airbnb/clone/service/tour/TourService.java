package project.airbnb.clone.service.tour;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.service.tour.workers.AccommodationSaveWorker;
import project.airbnb.clone.service.tour.workers.AreaListWorker;
import project.airbnb.clone.service.tour.workers.DetailCommonWorker;
import project.airbnb.clone.service.tour.workers.DetailImageWorker;
import project.airbnb.clone.service.tour.workers.DetailInfoWorker;
import project.airbnb.clone.service.tour.workers.DetailIntroWorker;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
@Profile("local")
@RequiredArgsConstructor
public class TourService {

    private final TourApiFacadeManager tourApiFacadeManager;
    private final TourRepositoryFacadeManager tourRepositoryFacadeManager;

    @Transactional
    public void fetchAccommodations(int pageNo, int numOfRows) {
        AreaListWorker worker = new AreaListWorker(tourApiFacadeManager, tourRepositoryFacadeManager);
        List<AccommodationProcessorDto> dtoList = worker.run(pageNo, numOfRows);

        dtoList.forEach(this::fillDto);

        saveAccommodations(dtoList);
    }

    private void fillDto(AccommodationProcessorDto dto) {
        new DetailCommonWorker(tourApiFacadeManager, dto).run();
        new DetailIntroWorker(tourApiFacadeManager, dto).run();
        new DetailInfoWorker(tourApiFacadeManager, dto).run();
        new DetailImageWorker(tourApiFacadeManager, dto).run();
    }

    private void saveAccommodations(List<AccommodationProcessorDto> dtoList) {
        AccommodationSaveWorker worker = new AccommodationSaveWorker(
                tourRepositoryFacadeManager,
                dtoList,
                this::hasMandatoryFields
        );

        worker.run();
    }

    private boolean hasMandatoryFields(AccommodationProcessorDto dto) {
        return hasText(dto.getTitle()) && hasText(dto.getContentId()) && dto.getModifiedTime() != null && hasText(dto.getSigunguCode()) &&
                hasText(dto.getAddress()) && dto.getMapX() != null && dto.getMapY() != null &&
                dto.hasThumbnail() && dto.hasAllPrices();
    }
}
