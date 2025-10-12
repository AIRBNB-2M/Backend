package project.airbnb.clone.controller.tour;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.airbnb.clone.service.tour.TourService;

@RestController
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/fetch-acc")
    public ResponseEntity<Void> fetchAccommodations(@RequestParam("pageNo") int pageNo,
                                                    @RequestParam("numOfRows") int numOfRows) {
        tourService.fetchAccommodations(pageNo, numOfRows);
        return ResponseEntity.ok().build();
    }
}
