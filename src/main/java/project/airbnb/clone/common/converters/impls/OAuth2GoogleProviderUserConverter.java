package project.airbnb.clone.common.converters.impls;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import project.airbnb.clone.common.converters.ProviderUserConverter;
import project.airbnb.clone.common.converters.ProviderUserRequest;
import project.airbnb.clone.consts.SocialType;
import project.airbnb.clone.model.OAuthUtils;
import project.airbnb.clone.model.ProviderUser;
import project.airbnb.clone.model.social.GoogleUser;

public class OAuth2GoogleProviderUserConverter implements ProviderUserConverter<ProviderUserRequest, ProviderUser> {

    @Override
    public ProviderUser converter(ProviderUserRequest providerUserRequest) {
        ClientRegistration clientRegistration = providerUserRequest.clientRegistration();

        if (clientRegistration == null || !clientRegistration.getRegistrationId().equals(SocialType.GOOGLE.getSocialName())) {
            return null;
        }

        return new GoogleUser(
                OAuthUtils.getMainAttributes(providerUserRequest.oAuth2User()),
                providerUserRequest.oAuth2User(),
                clientRegistration
        );
    }
}
