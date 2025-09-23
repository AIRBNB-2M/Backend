package project.airbnb.clone.controller.accommodation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.airbnb.clone.common.annotations.CurrentGuestId;
import project.airbnb.clone.dto.PageResponseDto;
import project.airbnb.clone.dto.accommodation.AccSearchCondDto;
import project.airbnb.clone.dto.accommodation.AccommodationPriceResDto;
import project.airbnb.clone.dto.accommodation.DetailAccommodationResDto;
import project.airbnb.clone.dto.accommodation.FilteredAccListResDto;
import project.airbnb.clone.dto.accommodation.MainAccResDto;
import project.airbnb.clone.dto.accommodation.ViewHistoryResDto;
import project.airbnb.clone.service.accommodation.AccommodationService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accommodations")
public class AccommodationController {

    private final AccommodationService accommodationService;

    @GetMapping
    public ResponseEntity<List<MainAccResDto>> getAccommodations(@CurrentGuestId(required = false) Long id) {
        List<MainAccResDto> result = accommodationService.getAccommodations(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponseDto<FilteredAccListResDto>> getFilteredPagingAccommodations(@ModelAttribute AccSearchCondDto searchDto,
                                                                                                  @CurrentGuestId(required = false) Long id,
                                                                                                  Pageable pageable) {
        PageResponseDto<FilteredAccListResDto> result = accommodationService.getFilteredPagingAccommodations(searchDto, id, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetailAccommodationResDto> getAccommodation(@PathVariable("id") Long accId,
                                                                      @CurrentGuestId(required = false) Long guestId) {
        DetailAccommodationResDto result = accommodationService.getDetailAccommodation(accId, guestId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/price")
    public ResponseEntity<AccommodationPriceResDto> getAccommodationPrice(@PathVariable("id") Long accId,
                                                                          @RequestParam("date") LocalDate date) {
        AccommodationPriceResDto result = accommodationService.getAccommodationPrice(accId, date);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ViewHistoryResDto>> getRecentViewAccommodations(@CurrentGuestId Long guestId) {
        List<ViewHistoryResDto> result = accommodationService.getRecentViewAccommodations(guestId);
        return ResponseEntity.ok(result);
    }
}
