package project.airbnb.clone.common.clients;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import project.airbnb.clone.dto.payment.PaymentConfirmDto;

@HttpExchange
public interface PaymentClient {

    @PostExchange("/confirm")
    JsonNode confirmPayment(@RequestBody PaymentConfirmDto paymentConfirmDTO);
}
