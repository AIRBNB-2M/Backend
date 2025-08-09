package project.airbnb.clone.common.converters.impls;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import project.airbnb.clone.common.converters.ProviderUserConverter;
import project.airbnb.clone.common.converters.ProviderUserRequest;
import project.airbnb.clone.consts.SocialType;
import project.airbnb.clone.model.OAuthUtils;
import project.airbnb.clone.model.ProviderUser;
import project.airbnb.clone.model.social.KakaoOidcUser;

public class OAuth2KakaoOidcProviderUserConverter implements ProviderUserConverter<ProviderUserRequest, ProviderUser> {

    @Override
    public ProviderUser converter(ProviderUserRequest providerUserRequest) {
        if (!providerUserRequest.clientRegistration().getRegistrationId().equals(SocialType.KAKAO.getSocialName())) {
            return null;
        }

        if (!(providerUserRequest.oAuth2User() instanceof OidcUser)) {
            return null;
        }

        return new KakaoOidcUser(
                OAuthUtils.getMainAttributes(providerUserRequest.oAuth2User()),
                providerUserRequest.oAuth2User(),
                providerUserRequest.clientRegistration()
        );
    }
}
