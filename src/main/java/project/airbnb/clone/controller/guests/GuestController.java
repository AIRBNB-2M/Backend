package project.airbnb.clone.controller.guests;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.airbnb.clone.service.guest.GuestService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/guests")
public class GuestController {

    private final GuestService guestService;

}
