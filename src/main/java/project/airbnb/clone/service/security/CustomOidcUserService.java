package project.airbnb.clone.service.security;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import project.airbnb.clone.common.converters.ProviderUserConverter;
import project.airbnb.clone.common.converters.ProviderUserRequest;
import project.airbnb.clone.model.PrincipalUser;
import project.airbnb.clone.model.ProviderUser;
import project.airbnb.clone.repository.guest.GuestRepository;
import project.airbnb.clone.service.guest.GuestService;

@Service
public class CustomOidcUserService extends AbstractOAuth2UserService
        implements OAuth2UserService<OidcUserRequest, OidcUser> {

    public CustomOidcUserService(GuestService guestService, GuestRepository guestRepository, ProviderUserConverter<ProviderUserRequest, ProviderUser> converter) {
        super(guestService, guestRepository, converter);
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        ClientRegistration clientRegistration = ClientRegistration.withClientRegistration(userRequest.getClientRegistration())
                                                                  .userNameAttributeName("sub")
                                                                  .build();

        OidcUserRequest oidcUserRequest = new OidcUserRequest(
                clientRegistration,
                userRequest.getAccessToken(),
                userRequest.getIdToken(),
                userRequest.getAdditionalParameters()
        );

        OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService = new OidcUserService();
        OidcUser oidcUser = oidcUserService.loadUser(oidcUserRequest);

        ProviderUserRequest providerUserRequest = new ProviderUserRequest(clientRegistration, oidcUser);

        ProviderUser providerUser = providerUser(providerUserRequest);

        //회원가입
        register(providerUser, userRequest);

        return new PrincipalUser(providerUser);
    }
}
