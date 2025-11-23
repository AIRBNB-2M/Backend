package project.airbnb.clone.controller.review;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.airbnb.clone.common.annotations.CurrentMemberId;
import project.airbnb.clone.dto.PageResponseDto;
import project.airbnb.clone.dto.review.MyReviewResDto;
import project.airbnb.clone.dto.review.UpdateReviewReqDto;
import project.airbnb.clone.service.review.ReviewService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/me")
    public ResponseEntity<PageResponseDto<MyReviewResDto>> getMyReviews(@CurrentMemberId Long memberId,
                                                                        @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponseDto<MyReviewResDto> response = reviewService.getMyReviews(memberId, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable("reviewId") Long reviewId,
                                          @RequestBody UpdateReviewReqDto reqDto,
                                          @CurrentMemberId Long memberId) {
        reviewService.updateReview(reviewId, reqDto, memberId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable("reviewId") Long reviewId,
                                          @CurrentMemberId Long memberId) {
        reviewService.deleteReview(reviewId, memberId);
        return ResponseEntity.ok().build();
    }
}
