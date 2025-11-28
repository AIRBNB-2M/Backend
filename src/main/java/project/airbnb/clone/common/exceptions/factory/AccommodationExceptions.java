package project.airbnb.clone.common.exceptions.factory;

import project.airbnb.clone.common.exceptions.BusinessException;
import project.airbnb.clone.common.exceptions.ErrorCode;

public abstract class AccommodationExceptions {

    public static BusinessException notFoundById(Long accommodationId) {
        return new BusinessException(
                ErrorCode.ACCOMMODATION_NOT_FOUND,
                "id=" + accommodationId + " 숙소 조회 실패"
        );
    }
}
