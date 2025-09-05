package project.airbnb.clone.service.tour;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.common.clients.TourApiClient;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.repository.AccommodationAmenityRepository;
import project.airbnb.clone.repository.AccommodationImageRepository;
import project.airbnb.clone.repository.AccommodationPriceRepository;
import project.airbnb.clone.repository.AccommodationRepository;
import project.airbnb.clone.repository.AmenityRepository;
import project.airbnb.clone.repository.SigunguCodeRepository;
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

    private final TourApiClient client;
    private final AmenityRepository amenityRepository;
    private final SigunguCodeRepository sigunguCodeRepository;
    private final AccommodationImageRepository imageRepository;
    private final AccommodationRepository accommodationRepository;
    private final AccommodationPriceRepository accommodationPriceRepository;
    private final AccommodationAmenityRepository accommodationAmenityRepository;

    @Transactional
    public void fetchAccommodations(int pageNo, int numOfRows) {
        AreaListWorker worker = new AreaListWorker(client, accommodationRepository);
        List<AccommodationProcessorDto> dtoList = worker.run(pageNo, numOfRows);

        dtoList.forEach(this::fillDto);

        saveAccommodations(dtoList);
    }

    private void fillDto(AccommodationProcessorDto dto) {
        new DetailCommonWorker(client, dto).run();
        new DetailIntroWorker(client, dto).run();
        new DetailInfoWorker(client, dto).run();
        new DetailImageWorker(client, dto).run();
    }

    private void saveAccommodations(List<AccommodationProcessorDto> dtoList) {
        AccommodationSaveWorker worker = new AccommodationSaveWorker(
                dtoList,
                amenityRepository,
                sigunguCodeRepository,
                imageRepository,
                accommodationRepository,
                accommodationPriceRepository,
                accommodationAmenityRepository,
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
