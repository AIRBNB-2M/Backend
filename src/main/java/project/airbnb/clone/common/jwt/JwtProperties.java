package project.airbnb.clone.common.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("jwt")
public class JwtProperties {

    private String secretKey;
    private TokenProperties accessToken;
    private TokenProperties refreshToken;

    @Data
    public static class TokenProperties {
        private int expiration;
    }

    public static final String AUTHORIZATION_HEADER = HttpHeaders.AUTHORIZATION;
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String REFRESH_TOKEN_KEY = "RefreshToken";
    public static final String PRINCIPAL_NAME = "principalName";
}
