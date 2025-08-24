package project.airbnb.clone.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.service.TourApiService;

@RestController
@RequiredArgsConstructor
public class TourApiController {

	private final TourApiService tourApiService;
	
	@PostMapping("/api/fetch-accommodations")
	public String fetchAndSaveAccommodations() {
		
		try {
            tourApiService.fetchAndSaveAccommodations();
            return "숙소 데이터 동기화(저장) 완료!";
        } catch (Exception e) {
            return "오류 발생: " + e.getMessage();
        }
	}
	
	@GetMapping("/api/accommodations")
	public List<Accommodation> getAllAccommodations() {
	    return tourApiService.getAllAccommodations();
	}

	@GetMapping("/api/accommodations/{id}")
	public ResponseEntity<Accommodation> getAccommodation(@PathVariable Long id) {
	    return tourApiService.getAccommodation(id)
	        .map(ResponseEntity::ok)
	        .orElse(ResponseEntity.notFound().build());
	}
}