package project.airbnb.clone.config.handlers.success;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import project.airbnb.clone.model.PrincipalUser;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        PrincipalUser principal = (PrincipalUser) authentication.getPrincipal();

        //TODO: JWT 발급 로직 필요
        log.debug("REST 인증 성공, 토큰 발급");
    }
}
