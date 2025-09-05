package project.airbnb.clone.service.tour;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.entity.AccommodationAmenity;
import project.airbnb.clone.entity.AccommodationImage;
import project.airbnb.clone.entity.AccommodationPrice;
import project.airbnb.clone.entity.Amenity;
import project.airbnb.clone.entity.SigunguCode;
import project.airbnb.clone.repository.AccommodationAmenityRepository;
import project.airbnb.clone.repository.AccommodationImageRepository;
import project.airbnb.clone.repository.AccommodationPriceRepository;
import project.airbnb.clone.repository.AccommodationRepository;
import project.airbnb.clone.repository.AmenityRepository;
import project.airbnb.clone.repository.SigunguCodeRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourRepositoryFacadeManager {

    private final AmenityRepository amenityRepository;
    private final SigunguCodeRepository sigunguCodeRepository;
    private final AccommodationImageRepository imageRepository;
    private final AccommodationRepository accommodationRepository;
    private final AccommodationPriceRepository accommodationPriceRepository;
    private final AccommodationAmenityRepository accommodationAmenityRepository;

    public Optional<Accommodation> findAccByContentId(String contentId) {
        return accommodationRepository.findByContentId(contentId);
    }

    public SigunguCode findSigunguCode(String code) {
        return sigunguCodeRepository.findById(code)
                                    .orElseThrow(() -> new EntityNotFoundException("Cannot found SigunguCode: " + code));
    }

    public Amenity findAmenityByName(String amenityName) {
        return amenityRepository.findByName(amenityName)
                                .orElseThrow(() -> new EntityNotFoundException("Cannot found Amenity: " + amenityName));
    }

    public Map<String, Accommodation> findByContentIdInToMap(List<String> contentIds) {
        return accommodationRepository.findByContentIdIn(contentIds)
                                      .stream()
                                      .collect(Collectors.toMap(Accommodation::getContentId, Function.identity()));
    }

    @Transactional
    public void saveEntities(List<Accommodation> accommodations,
                             List<AccommodationPrice> allPrices,
                             List<AccommodationAmenity> allAmenities,
                             List<AccommodationImage> allImages) {
        accommodationRepository.saveAll(accommodations);

        accommodationPriceRepository.deleteByAccommodationIn(accommodations);
        accommodationAmenityRepository.deleteByAccommodationIn(accommodations);
        imageRepository.deleteByAccommodationIn(accommodations);

        accommodationPriceRepository.saveAll(allPrices);
        accommodationAmenityRepository.saveAll(allAmenities);
        imageRepository.saveAll(allImages);
    }
}
