package project.airbnb.clone.service.guest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.consts.SocialType;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.model.ProviderUser;
import project.airbnb.clone.repository.guest.GuestRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(String registrationId, ProviderUser providerUser) {
        Guest guest = Guest.builder()
                           .name(providerUser.getUsername())
                           .email(providerUser.getEmail())
                           .number(providerUser.getNumber())
                           .birthDate(providerUser.getBirthDate())
                           .profileUrl(providerUser.getImageUrl())
                           .password(passwordEncoder.encode(providerUser.getPassword()))
                           .socialType(SocialType.from(registrationId))
                           .build();

        guestRepository.save(guest);
    }
}
