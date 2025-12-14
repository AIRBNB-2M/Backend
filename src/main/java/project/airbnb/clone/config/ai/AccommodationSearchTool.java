package project.airbnb.clone.config.ai;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class AccommodationSearchTool {

    @Tool(description = "사용자 메시지에서 명시적으로 언급된 숙소 검색 필터만 추출합니다. 언급되지 않은 항목은 null을 반환합니다.")
    public AccommodationFilterInfo extractSearchFilter(
            @ToolParam(description = "지역명 (예: 서울, 부산, 제주)") String region,
            @ToolParam(description = "최소 가격") Integer minPrice,
            @ToolParam(description = "최대 가격") Integer maxPrice,
            @ToolParam(description = "투숙 인원 수") Integer peopleCount) {

        return new AccommodationFilterInfo(region, minPrice, maxPrice, peopleCount);
    }
}
