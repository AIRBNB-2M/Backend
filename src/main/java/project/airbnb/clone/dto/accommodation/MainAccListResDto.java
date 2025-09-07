package project.airbnb.clone.dto.accommodation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 메인 화면 지역별 각 숙소 최소 정보
 */
@Data
@AllArgsConstructor
public class MainAccListResDto {
    private Long accommodationId;
    private String title;
    private int price;
    private long reservationCount;
    private double avgRate;
    private String thumbnailUrl;
    private boolean likedMe;

    @JsonIgnore
    private String areaCode;
}
