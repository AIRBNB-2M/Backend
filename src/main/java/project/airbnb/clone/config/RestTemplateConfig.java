package project.airbnb.clone.config;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
	
	@Bean
	public RestTemplate restTemplate() {
        RestTemplate rt = new RestTemplate();

        List<HttpMessageConverter<?>> converters = new ArrayList<>(rt.getMessageConverters());
        converters.removeIf(c -> c instanceof StringHttpMessageConverter);
        converters.add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        rt.setMessageConverters(converters);

        return rt;
    }
}
