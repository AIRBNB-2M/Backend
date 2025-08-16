package project.airbnb.clone.common.converters;

import lombok.Getter;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange(
        url = "https://api.github.com",
        accept = "application/vnd.github+json",
        headers = {
                "X-GitHub-Api-Version: 2022-11-28"
        }
)
public interface GitHubAppClient {

    @GetExchange("/user/emails")
    List<Response> getUserEmails(@RequestHeader("Authorization") String token);

    @Getter
    class Response {
        private String email;
        private boolean verified;
        private boolean primary;
        private String visibility;
    }
}
