package project.airbnb.clone.common.exceptions.factory;

import project.airbnb.clone.common.exceptions.BusinessException;
import project.airbnb.clone.common.exceptions.ErrorCode;

public abstract class ReviewExceptions {

    public static BusinessException notFoundReview(Long reviewId, Long memberId) {
        return new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                String.format("reviewId=%d, memberId=%d 후기 조회 실패", reviewId, memberId)
        );
    }

}
