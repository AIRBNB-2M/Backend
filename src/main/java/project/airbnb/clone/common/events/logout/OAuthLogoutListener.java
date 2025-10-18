package project.airbnb.clone.common.events.logout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import project.airbnb.clone.common.clients.KakaoAppClient;
import project.airbnb.clone.common.clients.KakaoAppClient.KakaoIdResponse;
import project.airbnb.clone.common.clients.NaverAppClient;
import project.airbnb.clone.common.clients.NaverAppClient.NaverResponse;
import project.airbnb.clone.consts.SocialType;
import project.airbnb.clone.model.PrincipalUser;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static project.airbnb.clone.common.jwt.JwtProperties.TOKEN_PREFIX;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class OAuthLogoutListener {

    private final KakaoAppClient kakaoAppClient;
    private final NaverAppClient naverAppClient;
    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @EventListener
    public void handleOAuthLogoutEvent(OAuthLogoutEvent event) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof PrincipalUser)) {
            return;
        }
        log.debug("OAuthLogoutListener.handleOAuthLogoutEvent: {}", event);

        SocialType socialType = event.socialType();
        switch (socialType) {
            case KAKAO -> {
                String accessToken = getAccessToken(socialType.getSocialName(), authentication);
                KakaoIdResponse response = kakaoAppClient.logout(TOKEN_PREFIX + accessToken);
                log.debug("kakao logout success: response={}", response);
            }
            case NAVER -> {
                String accessToken = getAccessToken(socialType.getSocialName(), authentication);
                String encodedToken = URLEncoder.encode(accessToken, StandardCharsets.UTF_8);

                NaverResponse response = naverAppClient.logout(encodedToken);
                log.debug("Naver logout success: response={}", response);
            }
            default -> log.debug("Not support logout API: {}", socialType);
        }
    }

    private String getAccessToken(String registrationId, Authentication authentication) {
        PrincipalUser principalUser = (PrincipalUser) authentication.getPrincipal();

        String principalName = principalUser.getPrincipalName();
        Assert.notNull(principalName, "PrincipalName Cannot be null");

        OAuth2AuthorizedClient authorizedClient = oAuth2AuthorizedClientService.loadAuthorizedClient(registrationId, principalName);
        return authorizedClient.getAccessToken().getTokenValue();
    }
}
