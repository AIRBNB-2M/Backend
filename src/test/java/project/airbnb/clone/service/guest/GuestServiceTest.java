package project.airbnb.clone.service.guest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import project.airbnb.clone.TestContainerSupport;
import project.airbnb.clone.common.exceptions.EmailAlreadyExistsException;
import project.airbnb.clone.consts.SocialType;
import project.airbnb.clone.dto.guest.DefaultProfileResDto;
import project.airbnb.clone.dto.guest.SignupRequestDto;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.model.ProviderUser;
import project.airbnb.clone.repository.jpa.GuestRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GuestServiceTest extends TestContainerSupport {

    @Autowired GuestService guestService;
    @Autowired GuestRepository guestRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        guestRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("OAuth 가입 성공")
    void oauthRegister() {
        //given
        ProviderUser providerUser = createProviderUser("google");

        //when
        guestService.register(providerUser);

        //then
        assertThat(guestRepository.existsByEmailAndSocialType(providerUser.getEmail(), SocialType.from(providerUser.getProvider()))).isTrue();

        Guest guest = guestRepository.getGuestByEmail(providerUser.getEmail());

        assertThat(guest).isNotNull();
        assertThat(guest.getPassword()).isNotEqualTo(providerUser.getPassword());
        assertThat(passwordEncoder.matches(providerUser.getPassword(), guest.getPassword())).isTrue();
    }

    @Test
    @DisplayName("OAuth 중복 가입 시도 시 저장되지 않는다.")
    void oauthRegister_duplicate() {
        //given
        ProviderUser providerUser = createProviderUser("google");
        guestService.register(providerUser);

        long first = guestRepository.count();

        //when
        guestService.register(providerUser);

        long second = guestRepository.count();

        //then
        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("OAuth 중복 이메일 가입 시도 시 예외가 발생한다.")
    void oauthRegister_throws() {
        //given
        ProviderUser googleUser = createProviderUser("google");
        guestService.register(googleUser);

        ProviderUser kakaoUser = createProviderUser("kakao");

        //when
        //then
        assertThatThrownBy(() -> guestService.register(kakaoUser))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("Email already exists : ");
    }

    @Test
    @DisplayName("REST 가입 성공")
    void restRegister() {
        //given
        SignupRequestDto requestDto = createRequestDto();

        //when
        guestService.register(requestDto);

        //then
        Guest guest = guestRepository.getGuestByEmail(requestDto.email());

        assertThat(guest).isNotNull();
        assertThat(guest.getPassword()).isNotEqualTo(requestDto.password());
        assertThat(passwordEncoder.matches(requestDto.password(), guest.getPassword())).isTrue();
    }

    @Test
    @DisplayName("REST 중복 이메일 가입 시도 시 예외가 발생한다.")
    void restRegister_throws() {
        //given
        SignupRequestDto requestDto = createRequestDto();
        guestService.register(requestDto);

        //when
        //then
        assertThatThrownBy(() -> guestService.register(requestDto))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("Email already exists : ");
    }

    @Test
    @DisplayName("사용자 기본 정보 조회")
    void getDefaultProfile() {
        //given
        Guest guest = saveAndGetGuest();

        //when
        DefaultProfileResDto result = guestService.getDefaultProfile(guest.getId());

        //then
        assertThat(result.name()).isEqualTo(guest.getName());
        assertThat(result.profileImageUrl()).isEqualTo(guest.getProfileUrl());
        assertThat(result.createdDate()).isEqualTo(guest.getCreatedAt().toLocalDate());
        assertThat(result.aboutMe()).isEqualTo(guest.getAboutMe());
    }

    private Guest saveAndGetGuest() {
        return guestRepository.save(Guest.createForTest());
    }

    private SignupRequestDto createRequestDto() {
        return new SignupRequestDto("Kamal Usman", "test@email.com", "01011223344", LocalDate.of(2002, 1, 1), "password");
    }

    private ProviderUser createProviderUser(String provider) {
        return new ProviderUser() {
            @Override
            public String getUsername() {
                return "Zin Xing";
            }

            @Override
            public String getPassword() {
                return "cddf211e-ec73-44e6-bb79-479565559089";
            }

            @Override
            public String getEmail() {
                return "test@email.com";
            }

            @Override
            public String getImageUrl() {
                return "186.50.16.55";
            }

            @Override
            public String getProvider() {
                return provider;
            }

            @Override
            public List<? extends GrantedAuthority> getAuthorities() {
                return List.of(new SimpleGrantedAuthority("ROLE_USER"));
            }
        };
    }
}