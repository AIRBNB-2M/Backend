package project.airbnb.clone.repository.query;

import com.querydsl.core.types.Projections;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import project.airbnb.clone.dto.review.MyReviewResDto;
import project.airbnb.clone.entity.Review;
import project.airbnb.clone.repository.query.support.CustomQuerydslRepositorySupport;

import static project.airbnb.clone.entity.QAccommodation.accommodation;
import static project.airbnb.clone.entity.QAccommodationImage.accommodationImage;
import static project.airbnb.clone.entity.QReservation.reservation;
import static project.airbnb.clone.entity.QReview.review;

@Repository
public class ReviewQueryRepository extends CustomQuerydslRepositorySupport {

    public ReviewQueryRepository() {
        super(Review.class);
    }

    public Page<MyReviewResDto> getMyReviews(Long memberId, Pageable pageable) {
        return applyPagination(pageable,
                contentQuery -> contentQuery
                        .select(Projections.constructor(MyReviewResDto.class,
                                review.id,
                                accommodation.id,
                                accommodationImage.imageUrl,
                                accommodation.title,
                                review.content,
                                review.rating,
                                review.createdAt
                        ))
                        .from(review)
                        .join(review.reservation, reservation)
                        .join(reservation.accommodation, accommodation)
                        .join(accommodationImage)
                        .on(accommodationImage.accommodation.eq(accommodation)
                                                            .and(accommodationImage.thumbnail.isTrue()))
                        .where(review.member.id.eq(memberId))
                ,
                countQuery -> countQuery.select(review.count())
                                        .from(review)
                                        .where(review.member.id.eq(memberId))
        );
    }
}
