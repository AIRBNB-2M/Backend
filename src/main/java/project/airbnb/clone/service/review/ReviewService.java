package project.airbnb.clone.service.review;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.dto.PageResponseDto;
import project.airbnb.clone.dto.review.MyReviewResDto;
import project.airbnb.clone.repository.query.ReviewQueryRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

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
}
