package project.airbnb.clone.common.exceptions.factory;

import project.airbnb.clone.common.exceptions.BusinessException;
import project.airbnb.clone.common.exceptions.ErrorCode;

public abstract class MemberExceptions {

    public static BusinessException notFoundByEmail(String email) {
        return new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND,
                "email=" + email + " 사용자 조회 실패"
        );
    }

    public static BusinessException notFoundById(Long memberId) {
        return new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND,
                "id=" + memberId + " 사용자 조회 실패"
        );
    }
}
