package project.airbnb.clone.dto.accommodation;

import java.util.List;

/**
 * 메인 화면 숙소 조회 DTO
 */
public record MainAccResDto(
        String areaName,
        String areaCode,
        List<MainAccListResDto> accommodations) {
}
