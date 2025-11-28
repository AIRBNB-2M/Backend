package project.airbnb.clone.common.exceptions.factory;

import project.airbnb.clone.common.exceptions.BusinessException;
import project.airbnb.clone.common.exceptions.ErrorCode;

public abstract class WishlistExceptions {

    public static BusinessException notFoundByIdAndMemberId(Long wishlistId, Long memberId) {
        return new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                String.format("id=%d 사용자 id=%d 위시리스트 조회 실패", wishlistId, memberId)
        );
    }

    public static BusinessException notFoundWishlistAccommodation(Long wishlistId, Long accommodationId, Long memberId) {
        return new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                String.format("id=%d 사용자의 id=%d 위시리스트 내 id=%d 숙소 조회 실패", memberId, wishlistId, accommodationId)
        );
    }

}
