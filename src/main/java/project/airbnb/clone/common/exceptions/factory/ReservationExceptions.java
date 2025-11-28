package project.airbnb.clone.common.exceptions.factory;

import project.airbnb.clone.common.exceptions.BusinessException;
import project.airbnb.clone.common.exceptions.ErrorCode;

public abstract class ReservationExceptions {

    public static BusinessException notFoundById(Long reservationId) {
        return new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                "id=" + reservationId + " 예약 조회 실패"
        );
    }

}
