package project.airbnb.clone.config.infra;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import project.airbnb.clone.common.clients.GitHubAppClient;

@Configuration
public class HttpClientConfig {

    @Bean
    public GitHubAppClient gitHubAppClient(RestClient.Builder builder) {
        RestClient restClient = builder.baseUrl("https://api.github.com").build();

        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                                      .build()
                                      .createClient(GitHubAppClient.class);
    }
}
