package project.airbnb.clone.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.airbnb.clone.service.ai.EmbeddingService;
import project.airbnb.clone.service.tour.TourService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final TourService tourService;
    private final EmbeddingService embeddingService;

    @PostMapping("/fetch-acc")
    public ResponseEntity<Void> fetchAccommodations(@RequestParam("pageNo") int pageNo,
                                                    @RequestParam("numOfRows") int numOfRows) {
        tourService.fetchAccommodations(pageNo, numOfRows);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/embed-accommodations")
    public ResponseEntity<Void> embedAccommodations(@PageableDefault(size = 50) Pageable pageable) {
        embeddingService.embedAccommodations(pageable);
        return ResponseEntity.ok().build();
    }
}
