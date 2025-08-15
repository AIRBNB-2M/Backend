package project.airbnb.clone.service.guest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.common.exceptions.EmailAlreadyExistsException;
import project.airbnb.clone.dto.guest.SignupRequestDto;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.model.ProviderUser;
import project.airbnb.clone.repository.guest.GuestRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * OAuth 가입
     */
    @Transactional
    public void register(ProviderUser providerUser) {
        validateExistsEmail(providerUser.getEmail());

        String encodePassword = encodePassword(providerUser.getPassword());
        Guest guest = providerUser.toEntity(encodePassword);

        guestRepository.save(guest);
    }

    /**
     * REST 가입
     */
    @Transactional
    public void register(SignupRequestDto signupRequestDto) {
        validateExistsEmail(signupRequestDto.email());

        String encodePassword = encodePassword(signupRequestDto.password());
        Guest guest = signupRequestDto.toEntity(encodePassword);

        guestRepository.save(guest);
    }

    private void validateExistsEmail(String email) {
        if (guestRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists : " + email);
        }
    }

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
