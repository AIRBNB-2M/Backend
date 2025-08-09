package project.airbnb.clone.service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.stereotype.Service;
import project.airbnb.clone.common.converters.ProviderUserConverter;
import project.airbnb.clone.common.converters.ProviderUserRequest;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.model.ProviderUser;
import project.airbnb.clone.repository.guest.GuestRepository;
import project.airbnb.clone.service.guest.GuestService;

@Service
@RequiredArgsConstructor
public abstract class AbstractOAuth2UserService {

    private final GuestService guestService;
    private final GuestRepository guestRepository;
    private final ProviderUserConverter<ProviderUserRequest, ProviderUser> providerUserConverter;

    protected ProviderUser providerUser(ProviderUserRequest providerUserRequest) {
        return providerUserConverter.converter(providerUserRequest);
    }

    protected void register(ProviderUser providerUser, OAuth2UserRequest userRequest) {
        Guest guest = getGuest(providerUser);

        if (guest == null) {
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            guestService.register(registrationId, providerUser);
        }
    }

    protected void register(ProviderUser providerUser) {
        Guest guest = getGuest(providerUser);

        if (guest == null) {
            guestService.register("none", providerUser);
        }
    }

    private Guest getGuest(ProviderUser providerUser) {
        return guestRepository.findByEmail(providerUser.getEmail()).orElse(null);
    }
}
