package project.airbnb.clone.controller.accommodation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.airbnb.clone.common.annotations.CurrentGuestId;
import project.airbnb.clone.dto.accommodation.AreaListResDto;
import project.airbnb.clone.dto.accommodation.MainAccListResDto;
import project.airbnb.clone.service.AccommodationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AccommodationController {

    private final AccommodationService accommodationService;

    @GetMapping("/accommodations")
    public ResponseEntity<List<AreaListResDto<MainAccListResDto>>> getAccommodations(@CurrentGuestId Long id) {
        List<AreaListResDto<MainAccListResDto>> result = accommodationService.getAccommodations(id);

        return ResponseEntity.ok(result);
    }
}
