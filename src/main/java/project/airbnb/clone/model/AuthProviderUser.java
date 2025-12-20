package project.airbnb.clone.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import project.airbnb.clone.entity.member.Member;

import java.util.List;

public record AuthProviderUser(Member member, String principalName) implements ProviderUser {

    @Override
    public String getUsername() {
        return member.getEmail();
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getEmail() {
        return member.getEmail();
    }

    @Override
    public String getImageUrl() {
        return member.getProfileUrl();
    }

    @Override
    public String getProvider() {
        return member.getSocialType().getSocialName();
    }

    @Override
    public List<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(member.getRole().getRoleName()));
    }

    @Override
    public String getPrincipalName() {
        return principalName;
    }

    @Override
    public Long getId() {
        return member.getId();
    }
}
