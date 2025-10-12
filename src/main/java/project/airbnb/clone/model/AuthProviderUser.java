package project.airbnb.clone.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import project.airbnb.clone.entity.Guest;

import java.util.List;

public record AuthProviderUser(Guest guest, String principalName) implements ProviderUser {

    @Override
    public String getUsername() {
        return guest.getEmail();
    }

    @Override
    public String getPassword() {
        return guest.getPassword();
    }

    @Override
    public String getEmail() {
        return guest.getEmail();
    }

    @Override
    public String getImageUrl() {
        return guest.getProfileUrl();
    }

    @Override
    public String getProvider() {
        return guest.getSocialType().getSocialName();
    }

    @Override
    public List<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(guest.getRole().getRoleName()));
    }

    @Override
    public String getPrincipalName() {
        return principalName;
    }

    @Override
    public Long getId() {
        return guest.getId();
    }
}
