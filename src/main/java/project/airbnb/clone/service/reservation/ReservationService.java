package project.airbnb.clone.service.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.common.exceptions.factory.MemberExceptions;
import project.airbnb.clone.common.exceptions.factory.ReservationExceptions;
import project.airbnb.clone.dto.reservation.PostReviewReqDto;
import project.airbnb.clone.entity.Member;
import project.airbnb.clone.entity.Reservation;
import project.airbnb.clone.entity.Review;
import project.airbnb.clone.repository.jpa.MemberRepository;
import project.airbnb.clone.repository.jpa.ReservationRepository;
import project.airbnb.clone.repository.jpa.ReviewRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public void postReview(Long reservationId, PostReviewReqDto reqDto, Long memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                                                       .orElseThrow(() -> ReservationExceptions.notFoundById(reservationId));
        Member member = memberRepository.findById(memberId)
                                        .orElseThrow(() -> MemberExceptions.notFoundById(memberId));

        reviewRepository.save(Review.create(reqDto.rating().doubleValue(), reqDto.content(), reservation, member));
    }
}
