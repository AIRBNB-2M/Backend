package project.airbnb.clone.config.security.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import project.airbnb.clone.config.security.handlers.failer.CustomAuthenticationEntryPoint;
import project.airbnb.clone.config.security.handlers.failer.RestAuthenticationFailureHandler;
import project.airbnb.clone.config.security.handlers.success.RestAuthenticationSuccessHandler;
import project.airbnb.clone.config.security.rest.RestApiDsl;

@Configuration
@RequiredArgsConstructor
public class RestSecurityConfigurer extends AbstractHttpConfigurer<RestSecurityConfigurer, HttpSecurity> {

    private final ObjectMapper objectMapper;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final RestAuthenticationSuccessHandler restAuthenticationSuccessHandler;
    private final RestAuthenticationFailureHandler restAuthenticationFailureHandler;

    @Override
    public void init(HttpSecurity http) throws Exception {
        http
                .with(new RestApiDsl<>(objectMapper), rest -> rest
                        .restSuccessHandler(restAuthenticationSuccessHandler)
                        .restFailureHandler(restAuthenticationFailureHandler)
                        .loginProcessingUrl("/api/auth/login")
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                );
    }
}
