package project.airbnb.clone.controller.accommodation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.airbnb.clone.common.annotations.CurrentMemberId;
import project.airbnb.clone.dto.PageResponseDto;
import project.airbnb.clone.dto.accommodation.*;
import project.airbnb.clone.service.accommodation.AccommodationService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accommodations")
public class AccommodationController {

    private final AccommodationService accommodationService;

    @GetMapping
    public ResponseEntity<List<MainAccResDto>> getAccommodations(@CurrentMemberId(required = false) Long memberId) {
        List<MainAccResDto> result = accommodationService.getAccommodations(memberId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponseDto<FilteredAccListResDto>> getFilteredPagingAccommodations(@ModelAttribute AccSearchCondDto searchDto,
                                                                                                  @CurrentMemberId(required = false) Long memberId,
                                                                                                  Pageable pageable) {
        PageResponseDto<FilteredAccListResDto> result = accommodationService.getFilteredPagingAccommodations(searchDto, memberId, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetailAccommodationResDto> getAccommodation(@PathVariable("id") Long accId,
                                                                      @CurrentMemberId(required = false) Long memberId) {
        DetailAccommodationResDto result = accommodationService.getDetailAccommodation(accId, memberId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/price")
    public ResponseEntity<AccommodationPriceResDto> getAccommodationPrice(@PathVariable("id") Long accId,
                                                                          @RequestParam("date") LocalDate date) {
        AccommodationPriceResDto result = accommodationService.getAccommodationPrice(accId, date);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ViewHistoryResDto>> getRecentViewAccommodations(@CurrentMemberId Long memberId) {
        List<ViewHistoryResDto> result = accommodationService.getRecentViewAccommodations(memberId);
        return ResponseEntity.ok(result);
    }
}
