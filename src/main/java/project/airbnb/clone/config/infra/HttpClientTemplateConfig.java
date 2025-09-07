package project.airbnb.clone.config.infra;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import project.airbnb.clone.common.clients.HolidayApiClient;
import project.airbnb.clone.common.clients.TourApiClient;
import project.airbnb.clone.service.tour.HttpClientTemplate;

@Configuration
public class HttpClientTemplateConfig {

    @Bean
    public HttpClientTemplate<TourApiClient> tourApiTemplate(TourApiClient client) {
        return new HttpClientTemplate<>(client);
    }

    @Bean
    public HttpClientTemplate<HolidayApiClient> holidayApiTemplate(HolidayApiClient client) {
        return new HttpClientTemplate<>(client);
    }
}
