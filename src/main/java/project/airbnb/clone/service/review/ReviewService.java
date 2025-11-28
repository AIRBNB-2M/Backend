package project.airbnb.clone.service.review;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.common.exceptions.factory.ReviewExceptions;
import project.airbnb.clone.dto.PageResponseDto;
import project.airbnb.clone.dto.review.MyReviewResDto;
import project.airbnb.clone.dto.review.UpdateReviewReqDto;
import project.airbnb.clone.entity.Review;
import project.airbnb.clone.repository.jpa.ReviewRepository;
import project.airbnb.clone.repository.query.ReviewQueryRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewQueryRepository reviewQueryRepository;

    public PageResponseDto<MyReviewResDto> getMyReviews(Long memberId, Pageable pageable) {
        Page<MyReviewResDto> result = reviewQueryRepository.getMyReviews(memberId, pageable);

        return PageResponseDto.<MyReviewResDto>builder()
                              .contents(result.getContent())
                              .pageNumber(pageable.getPageNumber())
                              .pageSize(pageable.getPageSize())
                              .total(result.getTotalElements())
                              .build();
    }

    @Transactional
    public void updateReview(Long reviewId, UpdateReviewReqDto reqDto, Long memberId) {
        Review review = getReview(reviewId, memberId);
        review.update(reqDto.rating().doubleValue(), reqDto.content());
    }

    @Transactional
    public void deleteReview(Long reviewId, Long memberId) {
        Review review = getReview(reviewId, memberId);
        reviewRepository.delete(review);
    }

    private Review getReview(Long reviewId, Long memberId) {
        return reviewRepository.findByIdAndMemberId(reviewId, memberId)
                               .orElseThrow(() -> ReviewExceptions.notFoundReview(reviewId, memberId));
    }
}
