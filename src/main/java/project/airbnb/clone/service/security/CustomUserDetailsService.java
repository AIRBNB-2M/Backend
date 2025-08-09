package project.airbnb.clone.service.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import project.airbnb.clone.common.converters.ProviderUserConverter;
import project.airbnb.clone.common.converters.ProviderUserRequest;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.model.PrincipalUser;
import project.airbnb.clone.model.ProviderUser;
import project.airbnb.clone.repository.guest.GuestRepository;
import project.airbnb.clone.service.guest.GuestService;

@Service
public class CustomUserDetailsService extends AbstractOAuth2UserService implements UserDetailsService {

    private final GuestRepository guestRepository;

    public CustomUserDetailsService(GuestService guestService, GuestRepository guestRepository, ProviderUserConverter<ProviderUserRequest, ProviderUser> converter) {
        super(guestService, guestRepository, converter);
        this.guestRepository = guestRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Guest guest = guestRepository.findByEmail(username).orElse(null);

        if (guest != null) {
            ProviderUserRequest providerUserRequest = new ProviderUserRequest(guest);
            ProviderUser providerUser = providerUser(providerUserRequest);

            register(providerUser);
            return new PrincipalUser(providerUser);
        }

        throw new UsernameNotFoundException("Cannot find guest for: " + username);
    }
}
