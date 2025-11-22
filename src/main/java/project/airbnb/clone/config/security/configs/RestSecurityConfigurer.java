package project.airbnb.clone.config.security.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import project.airbnb.clone.config.security.handlers.failer.CustomAuthenticationEntryPoint;
import project.airbnb.clone.config.security.handlers.failer.RestAuthenticationFailureHandler;
import project.airbnb.clone.config.security.handlers.success.RestAuthenticationSuccessHandler;
import project.airbnb.clone.config.security.rest.RestApiDsl;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@RequiredArgsConstructor
public class RestSecurityConfigurer extends AbstractHttpConfigurer<RestSecurityConfigurer, HttpSecurity> {

    private final JsonMapper jsonMapper;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final RestAuthenticationSuccessHandler restAuthenticationSuccessHandler;
    private final RestAuthenticationFailureHandler restAuthenticationFailureHandler;

    @Override
    public void init(HttpSecurity http) {
        http
                .with(new RestApiDsl<>(jsonMapper), rest -> rest
                        .restSuccessHandler(restAuthenticationSuccessHandler)
                        .restFailureHandler(restAuthenticationFailureHandler)
                        .loginProcessingUrl("/api/auth/login")
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                );
    }
}
