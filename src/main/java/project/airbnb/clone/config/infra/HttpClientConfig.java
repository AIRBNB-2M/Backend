package project.airbnb.clone.config.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import project.airbnb.clone.common.clients.GitHubAppClient;
import project.airbnb.clone.common.clients.KakaoAppClient;
import project.airbnb.clone.common.clients.NaverAppClient;
import project.airbnb.clone.common.clients.TourApiClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Configuration
public class HttpClientConfig {

    @Bean
    public GitHubAppClient gitHubAppClient(RestClient.Builder builder) {
        RestClient restClient = builder.baseUrl("https://api.github.com")
                                       .defaultHeader(ACCEPT, "application/vnd.github+json")
                                       .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                                       .build();

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

    @Bean
    public TourApiClient tourApiClient(RestClient.Builder builder,
                                       @Value("${tourapi.key}") String tourApiKey) {
        RestClient restClient = builder.baseUrl("https://apis.data.go.kr/B551011/KorService2")
                                       .defaultUriVariables(
                                               Map.of(
                                                       "serviceKey", tourApiKey,
                                                       "MobileApp", "AppTest",
                                                       "MobileOS", "ETC",
                                                       "type", "JSON",
                                                       "contentTypeId", "32"
                                               ))
                                       .build();

        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                                      .build()
                                      .createClient(TourApiClient.class);
    }

    @Bean
    public RestClient.Builder restClientBuilder() {
        HttpClient httpClient = HttpClient.newBuilder()
                                          .connectTimeout(Duration.ofSeconds(5))
                                          .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        return RestClient.builder()
                         .requestFactory(requestFactory)
                         .messageConverters(List.of(
                                 new MappingJackson2HttpMessageConverter(),
                                 new MappingJackson2XmlHttpMessageConverter())
                         );
    }
}
