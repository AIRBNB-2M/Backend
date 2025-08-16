package project.airbnb.clone.config.handlers.success;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import project.airbnb.clone.common.exceptions.EmailAlreadyExistsException;
import project.airbnb.clone.model.PrincipalUser;
import project.airbnb.clone.model.ProviderUser;
import project.airbnb.clone.service.guest.GuestService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuthAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final GuestService guestService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            PrincipalUser principal = (PrincipalUser) authentication.getPrincipal();
            ProviderUser providerUser = principal.providerUser();
            guestService.register(providerUser);
        } catch (EmailAlreadyExistsException ex) {
            OAuth2Error oAuth2Error = new OAuth2Error(ex.getMessage(), "Email Already Exists", null);
            throw new OAuth2AuthenticationException(oAuth2Error, ex);
        }
    }
}
