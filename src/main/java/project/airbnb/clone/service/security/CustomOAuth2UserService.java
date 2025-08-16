package project.airbnb.clone.service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import project.airbnb.clone.common.converters.GitHubAppClient;
import project.airbnb.clone.common.converters.ProviderUserConverter;
import project.airbnb.clone.common.converters.ProviderUserRequest;
import project.airbnb.clone.consts.SocialType;
import project.airbnb.clone.model.PrincipalUser;
import project.airbnb.clone.model.ProviderUser;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
    private final GitHubAppClient gitHubAppClient;
    private final ProviderUserConverter<ProviderUserRequest, ProviderUser> providerUserConverter;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        ClientRegistration clientRegistration = userRequest.getClientRegistration();

        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        if (SocialType.GITHUB.getSocialName().equals(clientRegistration.getRegistrationId())) {
            String token = generateBearerToken(userRequest);

            HashMap<String, Object> newAttributes = new HashMap<>(oAuth2User.getAttributes());
            String email = extractPrimaryEmail(token);
            newAttributes.put("email", email);

            oAuth2User = new DefaultOAuth2User(oAuth2User.getAuthorities(), newAttributes, "id");
        }

        ProviderUserRequest providerUserRequest = new ProviderUserRequest(clientRegistration, oAuth2User);
        ProviderUser providerUser = providerUserConverter.converter(providerUserRequest);

        return new PrincipalUser(providerUser);
    }

    private String extractPrimaryEmail(String token) {
        List<GitHubAppClient.Response> userEmails = gitHubAppClient.getUserEmails(token);

        return userEmails.stream()
                         .filter(response -> response.isPrimary() && response.isVerified())
                         .map(GitHubAppClient.Response::getEmail)
                         .findFirst()
                         .orElseThrow(() -> new OAuth2AuthenticationException("Github 계정에 verified & primary 이메일이 없습니다."));
    }

    private String generateBearerToken(OAuth2UserRequest userRequest) {
        String token = userRequest.getAccessToken().getTokenValue();
        return "Bearer " + token;
    }
}
