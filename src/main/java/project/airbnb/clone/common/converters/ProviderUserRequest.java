package project.airbnb.clone.common.converters;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;
import project.airbnb.clone.entity.Guest;

public record ProviderUserRequest(
        ClientRegistration clientRegistration,
        OAuth2User oAuth2User,
        Guest guest)
{
    public ProviderUserRequest(ClientRegistration clientRegistration, OAuth2User oAuth2User) {
        this(clientRegistration, oAuth2User, null);
    }

    public ProviderUserRequest(Guest guest) {
        this(null, null, guest);
    }
}
