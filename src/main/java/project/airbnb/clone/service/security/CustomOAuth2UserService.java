package project.airbnb.clone.service.security;

import lombok.extern.slf4j.Slf4j;
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
import project.airbnb.clone.repository.guest.GuestRepository;
import project.airbnb.clone.service.guest.GuestService;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class CustomOAuth2UserService extends AbstractOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final GitHubAppClient gitHubAppClient;

    public CustomOAuth2UserService(
            GuestService guestService,
            GuestRepository guestRepository,
            ProviderUserConverter<ProviderUserRequest, ProviderUser> converter,
            GitHubAppClient gitHubAppClient)
    {
        super(guestService, guestRepository, converter);
        this.gitHubAppClient = gitHubAppClient;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        ClientRegistration clientRegistration = userRequest.getClientRegistration();

        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);

        if (SocialType.GITHUB.getSocialName().equals(clientRegistration.getRegistrationId())) {
            log.debug("Github 사용자 이메일 요청 시작...");
            String token = generateBearerToken(userRequest);

            HashMap<String, Object> newAttributes = new HashMap<>(oAuth2User.getAttributes());
            String email = extractPrimaryEmail(token);
            newAttributes.put("email", email);

            oAuth2User = new DefaultOAuth2User(oAuth2User.getAuthorities(), newAttributes, "id");
        }

        ProviderUserRequest providerUserRequest = new ProviderUserRequest(clientRegistration, oAuth2User);

        ProviderUser providerUser = providerUser(providerUserRequest);

        //회원가입
        register(providerUser, userRequest);

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
