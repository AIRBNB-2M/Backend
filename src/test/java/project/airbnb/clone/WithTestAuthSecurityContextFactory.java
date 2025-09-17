package project.airbnb.clone;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.model.AuthProviderUser;
import project.airbnb.clone.model.PrincipalUser;

import java.util.List;

public class WithTestAuthSecurityContextFactory implements WithSecurityContextFactory<WithMockGuest> {

    @Override
    public SecurityContext createSecurityContext(WithMockGuest annotation) {
        Guest guest = Guest.builder().id(1L).build();

        PrincipalUser principalUser = new PrincipalUser(new AuthProviderUser(guest, "mock-principal"));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                principalUser, "", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        ));

        return context;
    }
}
