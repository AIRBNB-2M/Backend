package project.airbnb.clone.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.airbnb.clone.entity.accommodation.Accommodation;
import project.airbnb.clone.entity.accommodation.AccommodationImage;

import java.util.List;

public interface AccommodationImageRepository extends JpaRepository<AccommodationImage, Long> {

    @Query("""
            SELECT ai.imageUrl
            FROM AccommodationImage AS ai
            WHERE ai.accommodation = :accommodation AND ai.thumbnail = TRUE
            """)
    String findThumbnailUrl(@Param("accommodation") Accommodation accommodation);

    @Modifying(clearAutomatically = true)
    void deleteByAccommodationIn(List<Accommodation> accommodations);
}
