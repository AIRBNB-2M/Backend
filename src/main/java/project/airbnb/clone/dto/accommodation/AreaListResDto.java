package project.airbnb.clone.dto.accommodation;

import java.util.List;

/**
 * 메인 화면 & 지역별 숙소 조회 DTO
 *
 * @param areaName       지역명
 * @param accommodations 지역별 숙소 목록
 */
public record AreaListResDto<T>(
        String areaName,
        List<T> accommodations) {
}
