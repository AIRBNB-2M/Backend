package project.airbnb.clone.config.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import project.airbnb.clone.common.clients.GitHubAppClient;
import project.airbnb.clone.common.clients.KakaoAppClient;
import project.airbnb.clone.common.clients.NaverAppClient;

import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Configuration
public class HttpClientConfig {

    @Bean
    public GitHubAppClient gitHubAppClient(RestClient.Builder builder) {
        RestClient restClient = builder.baseUrl("https://api.github.com").build();

        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                                      .build()
                                      .createClient(GitHubAppClient.class);
    }

    @Bean
    public KakaoAppClient kakaoAppClient(RestClient.Builder builder) {
        RestClient restClient = builder.baseUrl("https://kapi.kakao.com/v1/user")
                                       .defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE + ";charset=utf-8")
                                       .build();

        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                                      .build()
                                      .createClient(KakaoAppClient.class);
    }

    @Bean
    public NaverAppClient naverAppClient(RestClient.Builder builder,
                                         @Value("${spring.security.oauth2.client.registration.naver.client-id}") String client_id,
                                         @Value("${spring.security.oauth2.client.registration.naver.client-secret}") String client_secret) {

        RestClient restClient = builder.baseUrl("https://nid.naver.com/oauth2.0/token")
                                       .defaultUriVariables(
                                               Map.of(
                                                       "client_id", client_id,
                                                       "client_secret", client_secret
                                               ))
                                       .build();

        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                                      .build()
                                      .createClient(NaverAppClient.class);
    }
}
