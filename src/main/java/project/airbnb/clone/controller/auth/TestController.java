package project.airbnb.clone.controller.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import project.airbnb.clone.model.PrincipalUser;

@Slf4j
@RestController
public class TestController {

    @GetMapping("/test")
    public void test(@AuthenticationPrincipal PrincipalUser principalUser) {
        log.info("/test");
    }
}
