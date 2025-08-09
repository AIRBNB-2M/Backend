package project.airbnb.clone.service.security;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import project.airbnb.clone.common.converters.ProviderUserConverter;
import project.airbnb.clone.common.converters.ProviderUserRequest;
import project.airbnb.clone.model.PrincipalUser;
import project.airbnb.clone.model.ProviderUser;
import project.airbnb.clone.repository.guest.GuestRepository;
import project.airbnb.clone.service.guest.GuestService;

@Service
public class CustomOAuth2UserService extends AbstractOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    public CustomOAuth2UserService(GuestService guestService, GuestRepository guestRepository, ProviderUserConverter<ProviderUserRequest, ProviderUser> converter) {
        super(guestService, guestRepository, converter);
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        ClientRegistration clientRegistration = userRequest.getClientRegistration();

        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);

        ProviderUserRequest providerUserRequest = new ProviderUserRequest(clientRegistration, oAuth2User);

        ProviderUser providerUser = providerUser(providerUserRequest);

        //회원가입
        register(providerUser, userRequest);

        return new PrincipalUser(providerUser);
    }
}
