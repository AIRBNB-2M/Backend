package project.airbnb.clone.config.security.handlers.success;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import project.airbnb.clone.common.exceptions.EmailAlreadyExistsException;
import project.airbnb.clone.dto.jwt.TokenResponse;
import project.airbnb.clone.model.PrincipalUser;
import project.airbnb.clone.model.ProviderUser;
import project.airbnb.clone.service.guest.GuestService;
import project.airbnb.clone.service.jwt.TokenService;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final GuestService guestService;
    private final TokenService tokenService;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            PrincipalUser principal = (PrincipalUser) authentication.getPrincipal();
            ProviderUser providerUser = principal.providerUser();

            guestService.register(providerUser);
            TokenResponse tokenResponse = tokenService.generateAndSendToken(providerUser.getEmail(), response);

            log.debug("OAuth 인증 성공, 토큰 발급");

            redirectStrategy.sendRedirect(request, response, "http://localhost:3000/auth/callback?token=" + tokenResponse.accessToken());

        } catch (EmailAlreadyExistsException ex) {
            OAuth2Error oAuth2Error = new OAuth2Error(ex.getMessage(), "Email Already Exists", null);
            throw new OAuth2AuthenticationException(oAuth2Error, ex);
        }
    }
}
