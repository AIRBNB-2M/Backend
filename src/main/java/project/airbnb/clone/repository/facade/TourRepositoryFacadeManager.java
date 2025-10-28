package project.airbnb.clone.repository.facade;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.entity.AccommodationAmenity;
import project.airbnb.clone.entity.AccommodationImage;
import project.airbnb.clone.entity.AccommodationPrice;
import project.airbnb.clone.entity.Amenity;
import project.airbnb.clone.entity.SigunguCode;
import project.airbnb.clone.repository.jpa.AccommodationAmenityRepository;
import project.airbnb.clone.repository.jpa.AccommodationImageRepository;
import project.airbnb.clone.repository.jpa.AccommodationPriceRepository;
import project.airbnb.clone.repository.jpa.AccommodationRepository;
import project.airbnb.clone.repository.jpa.AmenityRepository;
import project.airbnb.clone.repository.jpa.SigunguCodeRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourRepositoryFacadeManager {

    private final JdbcTemplate jdbcTemplate;
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
        long start = System.currentTimeMillis();

        accommodationRepository.saveAll(accommodations);

        accommodationPriceRepository.deleteByAccommodationIn(accommodations);
        accommodationAmenityRepository.deleteByAccommodationIn(accommodations);
        imageRepository.deleteByAccommodationIn(accommodations);

        batchInsertPrices(allPrices);
        batchInsertAmenities(allAmenities);
        batchInsertImages(allImages);

        long end = System.currentTimeMillis();
        int total = accommodations.size() + allPrices.size() + allAmenities.size() + allImages.size();
        log.debug("총 데이터 {}개 = 숙소 {}개 + 가격 {}개 + 편의시설 {}개 + 이미지 {}개 저장 {}ms 소요",
                total,
                accommodations.size(),
                allPrices.size(),
                allAmenities.size(),
                allImages.size(),
                (end - start));
    }

    private void batchInsertPrices(List<AccommodationPrice> prices) {
        if (prices.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO accommodation_prices(price, accommodation_id, season, day_type, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        int batchSize = prices.size();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        jdbcTemplate.batchUpdate(sql, prices, batchSize, (ps, entity) -> {
            ps.setInt(1, entity.getPrice());
            ps.setLong(2, entity.getAccommodation().getId());
            ps.setString(3, entity.getSeason().name());
            ps.setString(4, entity.getDayType().name());
            ps.setTimestamp(5, now);
            ps.setTimestamp(6, now);
        });
    }

    private void batchInsertAmenities(List<AccommodationAmenity> amenities) {
        if (amenities.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO accommodation_amenities(accommodation_id, amenity_id, created_at, updated_at) VALUES (?, ?, ?, ?)";
        int batchSize = amenities.size();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        jdbcTemplate.batchUpdate(sql, amenities, batchSize, (ps, entity) -> {
            ps.setLong(1, entity.getAccommodation().getId());
            ps.setLong(2, entity.getAmenity().getId());
            ps.setTimestamp(3, now);
            ps.setTimestamp(4, now);
        });
    }

    private void batchInsertImages(List<AccommodationImage> images) {
        if (images.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO accommodation_images(accommodation_id, thumbnail, image_url, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
        int batchSize = images.size();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        jdbcTemplate.batchUpdate(sql, images, batchSize, (ps, entity) -> {
            ps.setLong(1, entity.getAccommodation().getId());
            ps.setBoolean(2, entity.isThumbnail());
            ps.setString(3, entity.getImageUrl());
            ps.setTimestamp(4, now);
            ps.setTimestamp(5, now);
        });
    }
}
