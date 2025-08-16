package project.airbnb.clone.config.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import project.airbnb.clone.common.jwt.JwtProperties;
import project.airbnb.clone.config.handlers.failer.OAuthAuthenticationFailureHandler;
import project.airbnb.clone.config.handlers.failer.RestAuthenticationFailureHandler;
import project.airbnb.clone.config.handlers.success.RestAuthenticationSuccessHandler;
import project.airbnb.clone.config.handlers.success.OAuthAuthenticationSuccessHandler;
import project.airbnb.clone.config.rest.RestApiDsl;
import project.airbnb.clone.service.security.CustomOAuth2UserService;
import project.airbnb.clone.service.security.CustomOidcUserService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final CustomOidcUserService customOidcUserService;
    private final CustomOAuth2UserService customOAuth2UserService;

    private final RestAuthenticationSuccessHandler restAuthenticationSuccessHandler;
    private final OAuthAuthenticationSuccessHandler oAuthAuthenticationSuccessHandler;
    private final OAuthAuthenticationFailureHandler OAuthAuthenticationFailureHandler;
    private final RestAuthenticationFailureHandler restAuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                                .oidcUserService(customOidcUserService)
                        )
                        .successHandler(oAuthAuthenticationSuccessHandler)
                        .failureHandler(OAuthAuthenticationFailureHandler)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/signup").permitAll()
                        .anyRequest().permitAll()
                )

                .with(new RestApiDsl<>(objectMapper), rest -> rest
                        .restSuccessHandler(restAuthenticationSuccessHandler)
                        .restFailureHandler(restAuthenticationFailureHandler)
                        .loginProcessingUrl("/api/auth/login")
                )
        ;

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        configuration.addExposedHeader(JwtProperties.AUTHORIZATION_HEADER);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
