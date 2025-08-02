package project.airbnb.clone.consts;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Reason {
    DISSATISFACTION("에어비앤비 이용 경험이 불만족스럽습니다."),
    DISTRUST("에어비앤비의 데이터 처리 방식에 의구심이 듭니다."),
    DUPLICATE_ACCOUNT("중복 계정을 삭제하려고 합니다."),
    LOW_FREQUENCY("에어비앤비 이용 빈도가 낮습니다."),
    ETC("기타");

    private final String description;

    @JsonValue
    public String getDescription() {
        return description;
    }
}
