package project.airbnb.clone.service.guest;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.common.events.guest.GuestImageUploadEvent;
import project.airbnb.clone.common.exceptions.EmailAlreadyExistsException;
import project.airbnb.clone.consts.SocialType;
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
    private final ApplicationEventPublisher eventPublisher;

    /**
     * OAuth 가입
     */
    @Transactional
    public void register(ProviderUser providerUser) {
        String email = providerUser.getEmail();
        SocialType socialType = SocialType.from(providerUser.getProvider());

        if (guestRepository.existsByEmailAndSocialType(email, socialType)) {
            return;
        }

        validateExistsEmail(email);

        String encodePassword = encodePassword(providerUser.getPassword());
        Guest guest = providerUser.toEntity(encodePassword);

        guestRepository.save(guest);

        if (providerUser.getImageUrl() != null) {
            eventPublisher.publishEvent(new GuestImageUploadEvent(guest.getId(), providerUser.getImageUrl()));
        }
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
