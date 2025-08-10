package project.airbnb.clone.model.social;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;
import project.airbnb.clone.model.Attributes;
import project.airbnb.clone.model.OAuth2ProviderUser;

public class GithubUser extends OAuth2ProviderUser {

    public GithubUser(Attributes mainAttributes, OAuth2User oAuth2User, ClientRegistration clientRegistration) {
        super(mainAttributes.getMainAttributes(), oAuth2User, clientRegistration);
    }

    @Override
    public String getUsername() {
        return (String) getAttributes().get("name");
    }

    @Override
    public String getImageUrl() {
        return (String) getAttributes().get("avatar_url");
    }
}
