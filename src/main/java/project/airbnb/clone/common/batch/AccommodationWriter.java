package project.airbnb.clone.common.batch;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.dto.AccommodationProcessorDto;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.entity.AccommodationAmenity;
import project.airbnb.clone.entity.AccommodationImage;
import project.airbnb.clone.entity.Amenity;
import project.airbnb.clone.entity.SigunguCode;
import project.airbnb.clone.repository.AccommodationAmenityRepository;
import project.airbnb.clone.repository.AccommodationImageRepository;
import project.airbnb.clone.repository.AccommodationRepository;
import project.airbnb.clone.repository.AmenityRepository;
import project.airbnb.clone.repository.SigunguCodeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccommodationWriter implements ItemWriter<AccommodationProcessorDto> {

    private final AmenityRepository amenityRepository;
    private final SigunguCodeRepository sigunguCodeRepository;
    private final AccommodationImageRepository imageRepository;
    private final AccommodationRepository accommodationRepository;
    private final AccommodationAmenityRepository accommodationAmenityRepository;

    @Override
    @Transactional
    public void write(Chunk<? extends AccommodationProcessorDto> chunk) throws Exception {
        List<? extends AccommodationProcessorDto> items = chunk.getItems();

        for (AccommodationProcessorDto item : items) {

            if (!hasText(item.getContentId()) || item.getModifiedTime() == null || !hasText(item.getSigunguCode()) ||
                    !hasText(item.getAddress()) || item.getMapX() == null || item.getMapY() == null ||
                    hasNotThumbnail(item)) {
                return;
            }

            Accommodation acc = accommodationRepository.findByContentId(item.getContentId())
                                                       .orElseGet(Accommodation::new);
            SigunguCode sigunguCode = sigunguCodeRepository.findById(item.getSigunguCode())
                                                           .orElseThrow(() -> new EntityNotFoundException("Cannot found SigunguCode: " + item.getSigunguCode()));
            acc.updateOrInit(item, sigunguCode);

            if (item.isNew()) {
                accommodationRepository.save(acc);
            }

            updateAccommodationAmenity(item, acc);
            updateAccommodationImage(item, acc);
        }
    }

    private void updateAccommodationAmenity(AccommodationProcessorDto item, Accommodation acc) {
        accommodationAmenityRepository.deleteByAccommodation(acc);

        List<AccommodationAmenity> accommodationAmenities = new ArrayList<>();

        addEntityIfAvailable(accommodationAmenities, acc, item.getIntroAmenities());
        addEntityIfAvailable(accommodationAmenities, acc, item.getInfoAmenities());

        accommodationAmenityRepository.saveAll(accommodationAmenities);
    }

    private void addEntityIfAvailable(List<AccommodationAmenity> accommodationAmenities, Accommodation acc, Map<String, Boolean> amenities) {
        amenities.forEach((amenityName, available) -> {
            if (available) {
                Amenity amenity = amenityRepository.findByName(amenityName)
                                                   .orElseThrow(() -> new EntityNotFoundException("Cannot found Amenity: " + amenityName));

                accommodationAmenities.add(AccommodationAmenity.builder()
                                                               .accommodation(acc)
                                                               .amenity(amenity)
                                                               .build());
            }
        });
    }

    private void updateAccommodationImage(AccommodationProcessorDto item, Accommodation acc) {
        imageRepository.deleteByAccommodation(acc);

        List<AccommodationImage> accommodationImages = new ArrayList<>();

        String thumbnail = item.getThumbnailUrl();
        if (!hasText(thumbnail)) {
            if (!item.getOriginImgUrls().isEmpty()) {
                thumbnail = item.getOriginImgUrls().get(0);
            } else {
                thumbnail = item.getRoomImgUrls().get(0);
            }
        }

        accommodationImages.add(AccommodationImage.builder()
                                                  .accommodation(acc)
                                                  .imageUrl(thumbnail)
                                                  .thumbnail(true)
                                                  .build());

        addImageEntity(item.getOriginImgUrls(), thumbnail, accommodationImages, acc);
        addImageEntity(item.getRoomImgUrls(), thumbnail, accommodationImages, acc);

        imageRepository.saveAll(accommodationImages);
    }

    private void addImageEntity(List<String> item, String thumbnail, List<AccommodationImage> accommodationImages, Accommodation acc) {
        for (String imageUrl : item) {
            if (imageUrl.equals(thumbnail)) {
                continue;
            }
            accommodationImages.add(AccommodationImage.builder()
                                                      .accommodation(acc)
                                                      .imageUrl(imageUrl)
                                                      .thumbnail(false)
                                                      .build());
        }
    }

    private boolean hasNotThumbnail(AccommodationProcessorDto item) {
        return !hasText(item.getThumbnailUrl()) && item.getRoomImgUrls().isEmpty() && item.getOriginImgUrls().isEmpty();
    }
}
