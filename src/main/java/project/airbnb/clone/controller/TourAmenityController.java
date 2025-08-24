package project.airbnb.clone.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.airbnb.clone.entity.Amenity;
import project.airbnb.clone.service.TourAmenityService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TourAmenityController {

    private final TourAmenityService tourAmenityService;

    @PostMapping("/fetch-accommodation-amenities")
    public ResponseEntity<String> fetchAllAmenities() {
        try {
            int linked = tourAmenityService.fetchAndSaveAmenitiesForAllAccommodations();
            return ResponseEntity.ok("편의시설 매핑 완료: " + linked + "건 신규 링크");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("오류: " + e.getMessage());
        }
    }

    @PostMapping("/accommodations/content/{contentId}/fetch-amenities")
    public ResponseEntity<String> fetchByContentId(@PathVariable("contentId") String contentId) {
        try {
            int linked = tourAmenityService.fetchAndSaveAmenitiesByContentId(contentId);
            return ResponseEntity.ok("편의시설 매핑 완료: " + linked + "건");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("오류: " + e.getMessage());
        }
    }

    @GetMapping("/accommodations/{id}/amenities")
    public ResponseEntity<List<Amenity>> getAmenities(@PathVariable("id") Long id) {
        return ResponseEntity.ok(tourAmenityService.getAmenities(id));
    }
}