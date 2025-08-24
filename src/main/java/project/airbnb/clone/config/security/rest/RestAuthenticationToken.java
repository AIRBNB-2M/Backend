package project.airbnb.clone.config.security.rest;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class RestAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final Object credentials;

    public RestAuthenticationToken(Collection<? extends GrantedAuthority> authorities,
                                   Object principal, Object credentials) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    public RestAuthenticationToken(Object principal, Object credentials) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(false);
    }

    public static RestAuthenticationToken unauthenticated(Object principal, Object credentials) {
        return new RestAuthenticationToken(principal, credentials);
    }

    public static RestAuthenticationToken authenticated(Object principal, Object credentials,
                                                        Collection<? extends GrantedAuthority> authorities) {
        return new RestAuthenticationToken(authorities, principal, credentials);
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }
}