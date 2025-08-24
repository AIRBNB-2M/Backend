package project.airbnb.clone.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import project.airbnb.clone.entity.AccommodationImage;
import project.airbnb.clone.service.TourImageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TourImageController {

    private final TourImageService tourImageService;

    @PostMapping("/fetch-accommodation-images")
    public ResponseEntity<String> fetchAllImages() {
        try {
            int saved = tourImageService.fetchAndSaveImagesForAllAccommodations();
            return ResponseEntity.ok("숙소 이미지 저장 완료: " + saved + "건 신규/갱신");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("오류: " + e.getMessage());
        }
    }

    @GetMapping("/accommodations/{id}/images")
    public ResponseEntity<List<AccommodationImage>> getImages(@PathVariable Long id) {
        List<AccommodationImage> images = tourImageService.getImages(id);
        return ResponseEntity.ok(images);
    }

    @PostMapping("/accommodations/content/{contentId}/fetch-images")
    public ResponseEntity<String> fetchByContentId(@PathVariable("contentId") String contentId) {
        try {
            int saved = tourImageService.fetchAndSaveImagesByContentId(contentId);
            return ResponseEntity.ok("이미지 저장 완료: " + saved + "건");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("오류: " + e.getMessage());
        }
    }
}