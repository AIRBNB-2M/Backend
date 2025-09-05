package project.airbnb.clone.common.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.repository.AccommodationAmenityRepository;
import project.airbnb.clone.repository.AccommodationImageRepository;
import project.airbnb.clone.repository.AccommodationPriceRepository;
import project.airbnb.clone.repository.AccommodationRepository;
import project.airbnb.clone.repository.AmenityRepository;
import project.airbnb.clone.repository.SigunguCodeRepository;
import project.airbnb.clone.service.tour.workers.AccommodationSaveWorker;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccommodationWriter implements ItemWriter<AccommodationProcessorDto> {

    private final AmenityRepository amenityRepository;
    private final SigunguCodeRepository sigunguCodeRepository;
    private final AccommodationImageRepository imageRepository;
    private final AccommodationRepository accommodationRepository;
    private final AccommodationPriceRepository accommodationPriceRepository;
    private final AccommodationAmenityRepository accommodationAmenityRepository;

    @Override
    @Transactional
    public void write(Chunk<? extends AccommodationProcessorDto> chunk) throws Exception {
        AccommodationSaveWorker worker = new AccommodationSaveWorker(
                chunk.getItems(),
                amenityRepository,
                sigunguCodeRepository,
                imageRepository,
                accommodationRepository,
                accommodationPriceRepository,
                accommodationAmenityRepository,
                dto -> true
                //배치에서는 이전 process 단계에서 이미 필수값 검증된 상태에서 넘어오므로 항상 true
        );
        worker.run();
    }
}
