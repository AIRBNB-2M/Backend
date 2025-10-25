package project.airbnb.clone.service.review;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.dto.PageResponseDto;
import project.airbnb.clone.dto.review.MyReviewResDto;
import project.airbnb.clone.dto.review.UpdateReviewReqDto;
import project.airbnb.clone.entity.Review;
import project.airbnb.clone.repository.ReviewRepository;
import project.airbnb.clone.repository.query.ReviewQueryRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewQueryRepository reviewQueryRepository;

    public PageResponseDto<MyReviewResDto> getMyReviews(Long guestId, Pageable pageable) {
        Page<MyReviewResDto> result = reviewQueryRepository.getMyReviews(guestId, pageable);

        return PageResponseDto.<MyReviewResDto>builder()
                              .contents(result.getContent())
                              .pageNumber(pageable.getPageNumber())
                              .pageSize(pageable.getPageSize())
                              .total(result.getTotalElements())
                              .build();
    }

    @Transactional
    public void updateReview(Long reviewId, UpdateReviewReqDto reqDto, Long guestId) {
        Review review = reviewRepository.findByIdAndGuestId(reviewId, guestId)
                                        .orElseThrow(() -> new EntityNotFoundException("Cannot be found Review for id: " + reviewId + " and guestId: " + guestId));
        review.update(reqDto.rating().doubleValue(), reqDto.content());
    }
}
