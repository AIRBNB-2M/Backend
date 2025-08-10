package project.airbnb.clone.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import project.airbnb.clone.controller.guets.GuestController;
import project.airbnb.clone.service.guest.GuestService;

@Disabled
@WebMvcTest({
        GuestController.class
})
public abstract class ControllerTestSupport {

    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected MockMvc mockMvc;
    @MockitoBean protected GuestService guestService;

    protected String creatJson(Object dto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(dto);
    }
}
