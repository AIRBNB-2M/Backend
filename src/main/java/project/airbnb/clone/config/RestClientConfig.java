package project.airbnb.clone.config;

import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
	
	@Bean
    public RestClient restClient() {
        return RestClient.builder()
            .messageConverters(converters -> {
                converters.removeIf(c -> c instanceof StringHttpMessageConverter);
                converters.add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
            })
            .build();
    }
}
