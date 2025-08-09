package project.airbnb.clone.model.social;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;
import project.airbnb.clone.model.Attributes;
import project.airbnb.clone.model.OAuth2ProviderUser;

import java.util.Map;

public class KakaoUser extends OAuth2ProviderUser {

    private Map<String, Object> otherAttributes;

    public KakaoUser(Attributes attributes, OAuth2User oAuth2User, ClientRegistration clientRegistration) {
        super(attributes.getSubAttributes(), oAuth2User, clientRegistration);
        this.otherAttributes = attributes.getOtherAttributes();
    }

    @Override
    public String getUsername() {
        return (String) otherAttributes.get("nickname");
    }

    @Override
    public String getImageUrl() {
        return (String) otherAttributes.get("profile_image_url");
    }
}