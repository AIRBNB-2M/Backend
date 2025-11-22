package project.airbnb.clone.service.reservation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.dto.reservation.PostReviewReqDto;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.entity.Reservation;
import project.airbnb.clone.entity.Review;
import project.airbnb.clone.repository.jpa.ReviewRepository;
import project.airbnb.clone.repository.jpa.GuestRepository;
import project.airbnb.clone.repository.jpa.ReservationRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final GuestRepository guestRepository;
    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public void postReview(Long reservationId, PostReviewReqDto reqDto, Long guestId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                                                       .orElseThrow(() -> new EntityNotFoundException("Cannot be found Reservation for id: " + reservationId));
        Guest guest = guestRepository.findById(guestId)
                                     .orElseThrow(() -> new EntityNotFoundException("Cannot be found Guest for id: " + guestId));

        reviewRepository.save(Review.create(reqDto.rating().doubleValue(), reqDto.content(), reservation, guest));
    }
}
