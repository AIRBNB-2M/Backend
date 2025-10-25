package project.airbnb.clone.controller.review;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.airbnb.clone.common.annotations.CurrentGuestId;
import project.airbnb.clone.dto.PageResponseDto;
import project.airbnb.clone.dto.review.MyReviewResDto;
import project.airbnb.clone.service.review.ReviewService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/me")
    public ResponseEntity<PageResponseDto<MyReviewResDto>> getMyReviews(@CurrentGuestId Long guestId,
                                                                        Pageable pageable) {
        PageResponseDto<MyReviewResDto> response = reviewService.getMyReviews(guestId, pageable);
        return ResponseEntity.ok(response);
    }
}
