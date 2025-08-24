package project.airbnb.clone.common.clients;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange
public interface GitHubAppClient {

    @GetExchange("/user/emails")
    List<EmailInfoResponse> getUserEmails(@RequestHeader("Authorization") String token);

    record EmailInfoResponse(
            String email,
            boolean verified,
            boolean primary,
            String visibility) {
    }
}
