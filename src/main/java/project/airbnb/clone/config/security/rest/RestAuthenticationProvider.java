package project.airbnb.clone.config.security.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import project.airbnb.clone.model.PrincipalUser;

@Component
@RequiredArgsConstructor
public class RestAuthenticationProvider implements AuthenticationProvider {

    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = (String) authentication.getCredentials();

        PrincipalUser principalUser = (PrincipalUser) userDetailsService.loadUserByUsername(email);

        if (!passwordEncoder.matches(password, principalUser.getPassword())) {
            throw new AuthenticationServiceException("Invalid Password: " + password, new BadCredentialsException("Authentication Failed"));
        }

        return RestAuthenticationToken.authenticated(
                principalUser, null, principalUser.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(RestAuthenticationToken.class);
    }
}
