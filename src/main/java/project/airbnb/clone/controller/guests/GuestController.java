package project.airbnb.clone.controller.guests;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.airbnb.clone.common.annotations.CurrentGuestId;
import project.airbnb.clone.dto.guest.DefaultProfileResDto;
import project.airbnb.clone.service.guest.GuestService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/guests")
public class GuestController {

    private final GuestService guestService;

    @GetMapping("/me")
    public ResponseEntity<DefaultProfileResDto> getMyProfile(@CurrentGuestId Long guestId) {
        DefaultProfileResDto response = guestService.getDefaultProfile(guestId);
        return ResponseEntity.ok(response);
    }
}
