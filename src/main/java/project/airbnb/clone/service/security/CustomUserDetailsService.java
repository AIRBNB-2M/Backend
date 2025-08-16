package project.airbnb.clone.service.security;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final GuestRepository guestRepository;
    private final ProviderUserConverter<ProviderUserRequest, ProviderUser> providerUserConverter;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Guest guest = guestRepository.findByEmail(username)
                                     .orElseThrow(() -> new UsernameNotFoundException("Cannot find guest for: " + username));

        ProviderUserRequest providerUserRequest = new ProviderUserRequest(guest);
        ProviderUser providerUser = providerUserConverter.converter(providerUserRequest);

        return new PrincipalUser(providerUser);
    }
}
