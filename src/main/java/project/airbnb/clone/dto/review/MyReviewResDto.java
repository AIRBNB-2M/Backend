package project.airbnb.clone.dto.review;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MyReviewResDto(
        Long reviewId,
        Long accommodationId,
        String thumbnailUrl,
        String title,
        String content,
        double rate,
        LocalDate createdDate) {

    public MyReviewResDto(Long reviewId, Long accommodationId, String thumbnailUrl, String title, String content, double rate, LocalDateTime createdDate) {
        this(reviewId, accommodationId, thumbnailUrl, title, content, rate, createdDate.toLocalDate());
    }
}
