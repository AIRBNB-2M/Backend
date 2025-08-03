package project.airbnb.clone.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;

public interface ProviderUser {

    String getId();
    String getUsername();
    String getPassword();
    String getEmail();
    String getImageUrl();

    List<? extends GrantedAuthority> getAuthorities();

    default String getProvider() {
        return "none";
    }

    default Map<String, Object> getAttributes() {
        return null;
    }

    default OAuth2User getOAuth2User() {
        return null;
    }
}
