package project.airbnb.clone.common.clients;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(headers = "Accept: application/json")
public interface HolidayApiClient {

    @GetExchange("/getRestDeInfo?serviceKey={serviceKey}&_type={type}&numOfRows={numOfRows}")
    JsonNode getHolidays(@RequestParam("solYear") int year);
}
