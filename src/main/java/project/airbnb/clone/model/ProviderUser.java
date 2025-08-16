package project.airbnb.clone.model;

import org.springframework.security.core.GrantedAuthority;
import project.airbnb.clone.consts.SocialType;
import project.airbnb.clone.entity.Guest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ProviderUser {

    String getUsername();
    String getPassword();
    String getEmail();
    String getImageUrl();
    String getProvider();
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

    default Guest toEntity(String encodePassword) {
        return Guest.builder()
                    .name(getUsername())
                    .email(getEmail())
                    .number(getNumber())
                    .birthDate(getBirthDate())
                    .profileUrl(getImageUrl())
                    .password(encodePassword)
                    .socialType(SocialType.from(getProvider()))
                    .build();
    }
}
