package project.airbnb.clone.controller.reservation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.airbnb.clone.common.annotations.CurrentGuestId;
import project.airbnb.clone.dto.reservation.PostReviewReqDto;
import project.airbnb.clone.service.reservation.ReservationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/{reservationId}/reviews")
    public ResponseEntity<?> postReview(@PathVariable("reservationId") Long reservationId,
                                        @Valid @RequestBody PostReviewReqDto reqDto,
                                        @CurrentGuestId Long guestId) {
        reservationService.postReview(reservationId, reqDto, guestId);
        return ResponseEntity.status(201).build();
    }
}
