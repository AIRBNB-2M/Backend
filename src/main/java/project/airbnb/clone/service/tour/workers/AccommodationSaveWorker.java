package project.airbnb.clone.service.tour.workers;

import project.airbnb.clone.consts.DayType;
import project.airbnb.clone.consts.Season;
import project.airbnb.clone.dto.accommodation.AccommodationProcessorDto;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.entity.AccommodationAmenity;
import project.airbnb.clone.entity.AccommodationImage;
import project.airbnb.clone.entity.AccommodationPrice;
import project.airbnb.clone.entity.Amenity;
import project.airbnb.clone.entity.SigunguCode;
import project.airbnb.clone.repository.facade.TourRepositoryFacadeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.springframework.util.StringUtils.hasText;

public record AccommodationSaveWorker(
        TourRepositoryFacadeManager tourRepositoryFacadeManager,
        List<? extends AccommodationProcessorDto> dtoList,
        Predicate<AccommodationProcessorDto> validator) implements Runnable {

    @Override
    public void run() {
        List<Accommodation> accommodations = new ArrayList<>();
        List<AccommodationAmenity> allAmenities = new ArrayList<>();
        List<AccommodationImage> allImages = new ArrayList<>();
        List<AccommodationPrice> allPrices = new ArrayList<>();

        for (AccommodationProcessorDto dto : dtoList) {
            if (!validator.test(dto)) continue;

            Accommodation acc = tourRepositoryFacadeManager.findAccByContentId(dto.getContentId()).orElseGet(Accommodation::new);
            SigunguCode sigunguCode = tourRepositoryFacadeManager.findSigunguCode(dto.getSigunguCode());

            acc.updateOrInit(dto, sigunguCode);

            accommodations.add(acc);
            addAccommodationAmenity(dto, acc, allAmenities);
            addAccommodationImage(dto, acc, allImages);
            addAccommodationPrice(dto, acc, allPrices);
        }

        tourRepositoryFacadeManager.saveEntities(accommodations, allPrices, allAmenities, allImages);
    }

    private void addAccommodationAmenity(AccommodationProcessorDto dto, Accommodation acc, List<AccommodationAmenity> allAmenities) {
        addEntityIfAvailable(allAmenities, acc, dto.getIntroAmenities());
        addEntityIfAvailable(allAmenities, acc, dto.getInfoAmenities());
    }

    private void addEntityIfAvailable(List<AccommodationAmenity> allAmenities, Accommodation acc, Map<String, Boolean> amenities) {
        amenities.forEach((amenityName, available) -> {
            if (available) {
                Amenity amenity = tourRepositoryFacadeManager.findAmenityByName(amenityName);
                allAmenities.add(AccommodationAmenity.builder()
                                                     .accommodation(acc)
                                                     .amenity(amenity)
                                                     .build());
            }
        });
    }

    private void addAccommodationImage(AccommodationProcessorDto item, Accommodation acc, List<AccommodationImage> allImages) {
        String thumbnailUrl = item.getThumbnailUrl();

        if (!hasText(thumbnailUrl)) {
            if (!item.getOriginImgUrls().isEmpty()) {
                thumbnailUrl = item.getOriginImgUrls().get(0);
            } else {
                thumbnailUrl = item.getRoomImgUrls().get(0);
            }
        }

        allImages.add(AccommodationImage.builder()
                                        .accommodation(acc)
                                        .imageUrl(thumbnailUrl)
                                        .thumbnail(true)
                                        .build());

        addImageEntity(item.getOriginImgUrls(), thumbnailUrl, allImages, acc);
        addImageEntity(item.getRoomImgUrls(), thumbnailUrl, allImages, acc);
    }

    private void addImageEntity(List<String> item, String thumbnail, List<AccommodationImage> allImages, Accommodation acc) {
        for (String imageUrl : item) {
            if (imageUrl.equals(thumbnail)) {
                continue;
            }
            allImages.add(AccommodationImage.builder()
                                            .accommodation(acc)
                                            .imageUrl(imageUrl)
                                            .thumbnail(false)
                                            .build());
        }
    }

    private void addAccommodationPrice(AccommodationProcessorDto dto, Accommodation acc, List<AccommodationPrice> allPrices) {
        for (Season season : Season.values()) {
            for (DayType dayType : DayType.values()) {
                allPrices.add(AccommodationPrice.builder()
                                                .accommodation(acc)
                                                .season(season)
                                                .dayType(dayType)
                                                .price(dto.getPrice(season, dayType))
                                                .build());
            }
        }
    }
}
