package project.airbnb.clone.controller.guests;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import project.airbnb.clone.controller.RestDocsTestSupport;
import project.airbnb.clone.service.guest.GuestService;

@WebMvcTest(GuestController.class)
class GuestControllerTest extends RestDocsTestSupport {

    private static final String GUEST_API_TAG = "Guest-API";

    @MockitoBean GuestService guestService;

}