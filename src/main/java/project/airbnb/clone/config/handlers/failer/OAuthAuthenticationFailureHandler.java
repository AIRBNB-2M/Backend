package project.airbnb.clone.config.handlers.failer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import project.airbnb.clone.common.exceptions.EmailAlreadyExistsException;

import java.io.IOException;

@Slf4j
@Component
public class OAuthAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.debug("인증 실패 : {}", exception.getMessage());

        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        Throwable cause = exception.getCause();

        if (cause instanceof EmailAlreadyExistsException) {
            statusCode = HttpStatus.CONFLICT.value();
        }

        //TODO: 운영 환경에서 프론트엔드 배포 주소 연결
        redirectStrategy.sendRedirect(request, response, "http://localhost:3000/auth/callback?error=" + statusCode);
    }
}
