package project.airbnb.clone.common.events.logout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import project.airbnb.clone.common.clients.KakaoAppClient;
import project.airbnb.clone.common.clients.KakaoAppClient.KakaoIdResponse;
import project.airbnb.clone.consts.SocialType;
import project.airbnb.clone.model.PrincipalUser;

import static project.airbnb.clone.common.jwt.JwtProperties.TOKEN_PREFIX;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthLogoutListener {

    private final KakaoAppClient kakaoAppClient;
    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @EventListener
    public void handleOAuthLogoutEvent(OAuthLogoutEvent event) {
        log.debug("OAuthLogoutListener.handleOAuthLogoutEvent: {}", event);

        SocialType socialType = event.socialType();
        switch (socialType) {
            case KAKAO -> {
                String accessToken = getAccessToken(socialType.getSocialName());
                KakaoIdResponse response = kakaoAppClient.logout(TOKEN_PREFIX + accessToken);
                log.debug("kakao logout success: response={}", response);
            }
            case NAVER -> {
                String accessToken = getAccessToken(socialType.getSocialName());
            }
            default -> log.debug("Not support logout API: {}", socialType);
        }
    }

    private String getAccessToken(String registrationId) {
        Authentication authentication = SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication();
        PrincipalUser principalUser = (PrincipalUser) authentication.getPrincipal();

        String principalName = principalUser.getPrincipalName();

        Assert.notNull(principalUser, "PrincipalName Cannot be null");

        OAuth2AuthorizedClient authorizedClient = oAuth2AuthorizedClientService.loadAuthorizedClient(registrationId, principalName);
        return authorizedClient.getAccessToken().getTokenValue();
    }
}
