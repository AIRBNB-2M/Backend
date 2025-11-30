package project.airbnb.clone.controller.reservation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.airbnb.clone.common.annotations.CurrentMemberId;
import project.airbnb.clone.dto.reservation.PostReservationReqDto;
import project.airbnb.clone.dto.reservation.PostReservationResDto;
import project.airbnb.clone.dto.reservation.PostReviewReqDto;
import project.airbnb.clone.service.reservation.ReservationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/{accommodationId}")
    public ResponseEntity<PostReservationResDto> postReservation(@CurrentMemberId Long memberId,
                                                                 @PathVariable("accommodationId") Long accommodationId,
                                                                 @RequestBody PostReservationReqDto reqDto) {
        PostReservationResDto response = reservationService.postReservation(memberId, accommodationId, reqDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{reservationId}/reviews")
    public ResponseEntity<?> postReview(@PathVariable("reservationId") Long reservationId,
                                        @Valid @RequestBody PostReviewReqDto reqDto,
                                        @CurrentMemberId Long memberId) {
        reservationService.postReview(reservationId, reqDto, memberId);
        return ResponseEntity.status(201).build();
    }
}
