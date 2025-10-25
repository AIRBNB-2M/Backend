package project.airbnb.clone.repository.query;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import project.airbnb.clone.dto.review.MyReviewResDto;

import java.util.List;

import static project.airbnb.clone.entity.QAccommodation.accommodation;
import static project.airbnb.clone.entity.QAccommodationImage.accommodationImage;
import static project.airbnb.clone.entity.QReservation.reservation;
import static project.airbnb.clone.entity.QReview.review;

@Repository
@RequiredArgsConstructor
public class ReviewQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<MyReviewResDto> getMyReviews(Long guestId, Pageable pageable) {
        List<MyReviewResDto> content = queryFactory.select(Projections.constructor(MyReviewResDto.class,
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
                                                   .where(review.guest.id.eq(guestId))
                                                   .offset(pageable.getOffset())
                                                   .limit(pageable.getPageSize())
                                                   .orderBy(review.id.desc())
                                                   .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(review.count())
                                                .from(review)
                                                .where(review.guest.id.eq(guestId));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
