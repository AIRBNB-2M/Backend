package project.airbnb.clone.service.guest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    /**
     * OAuth 가입
     * @param registrationId 소셜 정보
     */
    @Transactional
    public void register(String registrationId, ProviderUser providerUser) {
        Guest guest = Guest.builder()
                           .name(providerUser.getUsername())
                           .email(providerUser.getEmail())
                           .number(providerUser.getNumber())
                           .birthDate(providerUser.getBirthDate())
                           .profileUrl(providerUser.getImageUrl())
                           .password(encodePassword(providerUser.getPassword()))
                           .socialType(SocialType.from(registrationId))
                           .build();

        guestRepository.save(guest);
    }

    /**
     * REST 가입
     */
    @Transactional
    public void register(SignupRequestDto signupRequestDto) {
        if (guestRepository.existsByEmail(signupRequestDto.email())) {
            throw new EmailAlreadyExistsException("Email already exists : " + signupRequestDto.email());
        }

        String encodePassword = encodePassword(signupRequestDto.password());
        Guest guest = signupRequestDto.toEntity(encodePassword);

        guestRepository.save(guest);
    }

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
