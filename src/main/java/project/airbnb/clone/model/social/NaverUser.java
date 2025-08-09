package project.airbnb.clone.model.social;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;
import project.airbnb.clone.model.Attributes;
import project.airbnb.clone.model.OAuth2ProviderUser;

import java.time.LocalDate;

public class NaverUser extends OAuth2ProviderUser {

    public NaverUser(Attributes attributes, OAuth2User oAuth2User, ClientRegistration clientRegistration) {
        super(attributes.getSubAttributes(), oAuth2User, clientRegistration);
    }

    @Override
    public String getUsername() {
        return (String) getAttributes().get("name");
    }

    @Override
    public String getImageUrl() {
        return (String) getAttributes().get("profile_image");
    }

    @Override
    public LocalDate getBirthDate() {
        String birthyear = (String) getAttributes().get("birthyear");
        String birthday = (String) getAttributes().get("birthday");

        String[] split = birthday.split("-");

        int year = Integer.parseInt(birthyear);
        int month = Integer.parseInt(split[0]);
        int day = Integer.parseInt(split[1]);

        return LocalDate.of(year, month, day);
    }

    @Override
    public String getNumber() {
        String number = (String) getAttributes().get("mobile");
        return number.replaceAll("-", "");
    }
}