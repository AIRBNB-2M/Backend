package project.airbnb.clone.controller.tour;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.airbnb.clone.service.tour.TourService;

@Profile("local")
@RestController
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

    @PostMapping("/fetch-acc")
    public ResponseEntity<Void> fetchAccommodations(@RequestParam("pageNo") int pageNo,
                                                    @RequestParam("numOfRows") int numOfRows) {
        tourService.fetchAccommodations(pageNo, numOfRows);
        return ResponseEntity.ok().build();
    }
}
