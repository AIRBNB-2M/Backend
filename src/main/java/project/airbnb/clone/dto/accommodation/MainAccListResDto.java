package project.airbnb.clone.dto.accommodation;

/**
 * 메인 화면 지역별 각 숙소 최소 정보
 */
public record MainAccListResDto(
        Long accommodationId,
        String title,
        int price,
        double avgRate,
        String thumbnailUrl,
        boolean likedMe) {

    public static MainAccListResDto from(MainAccListQueryDto queryDto) {
        return new MainAccListResDto(
                queryDto.accommodationId(),
                queryDto.title(),
                queryDto.price(),
                queryDto.avgRate(),
                queryDto.thumbnailUrl(),
                queryDto.likedMe()
        );
    }
}
