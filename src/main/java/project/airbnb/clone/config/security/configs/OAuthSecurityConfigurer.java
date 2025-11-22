package project.airbnb.clone.config.security.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import project.airbnb.clone.config.security.handlers.failer.OAuthAuthenticationFailureHandler;
import project.airbnb.clone.config.security.handlers.success.OAuthAuthenticationSuccessHandler;
import project.airbnb.clone.service.security.CustomOAuth2UserService;
import project.airbnb.clone.service.security.CustomOidcUserService;

@Configuration
@RequiredArgsConstructor
public class OAuthSecurityConfigurer extends AbstractHttpConfigurer<OAuthSecurityConfigurer, HttpSecurity> {

    private final CustomOidcUserService customOidcUserService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuthAuthenticationSuccessHandler oAuthAuthenticationSuccessHandler;
    private final OAuthAuthenticationFailureHandler OAuthAuthenticationFailureHandler;

    @Override
    public void init(HttpSecurity http) {
        http
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                                .oidcUserService(customOidcUserService)
                        )
                        .successHandler(oAuthAuthenticationSuccessHandler)
                        .failureHandler(OAuthAuthenticationFailureHandler)
                );
    }
}
