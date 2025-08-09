package project.airbnb.clone.model;

import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ProviderUser {

    String getUsername();
    String getPassword();
    String getEmail();
    String getImageUrl();
    List<? extends GrantedAuthority> getAuthorities();

    default Map<String, Object> getAttributes() {
        return null;
    }

    default LocalDate getBirthDate() {
        return null;
    }

    default String getNumber() {
        return null;
    }
}
