package project.airbnb.clone.config.infra;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import project.airbnb.clone.common.clients.GitHubAppClient;
import project.airbnb.clone.common.clients.KakaoAppClient;

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
}
