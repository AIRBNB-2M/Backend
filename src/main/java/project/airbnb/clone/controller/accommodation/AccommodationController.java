package project.airbnb.clone.controller.accommodation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.airbnb.clone.common.annotations.CurrentGuestId;
import project.airbnb.clone.dto.PageResponseDto;
import project.airbnb.clone.dto.accommodation.AccSearchCondDto;
import project.airbnb.clone.dto.accommodation.FilteredAccListResDto;
import project.airbnb.clone.dto.accommodation.MainAccResDto;
import project.airbnb.clone.service.AccommodationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AccommodationController {

    private final AccommodationService accommodationService;

    @GetMapping("/accommodations")
    public ResponseEntity<List<MainAccResDto>> getAccommodations(@CurrentGuestId(required = false) Long id) {
        List<MainAccResDto> result = accommodationService.getAccommodations(id);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/accommodations/search")
    public ResponseEntity<PageResponseDto<FilteredAccListResDto>> getFilteredPagingAccommodations(@ModelAttribute AccSearchCondDto searchDto,
                                                                                                  @CurrentGuestId(required = false) Long id,
                                                                                                  Pageable pageable) {
        PageResponseDto<FilteredAccListResDto> result = accommodationService.getFilteredPagingAccommodations(searchDto, id, pageable);
        return ResponseEntity.ok(result);
    }
}
