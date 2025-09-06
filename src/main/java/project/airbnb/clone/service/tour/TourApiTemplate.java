package project.airbnb.clone.service.tour;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import project.airbnb.clone.common.clients.TourApiClient;
import project.airbnb.clone.dto.TourApiResponse;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class TourApiTemplate {

    private final TourApiClient client;

    public List<Map<String, String>> fetchItems(Function<TourApiClient, JsonNode> apiCall,
                                                Consumer<List<Map<String, String>>> postProcessor) {
        JsonNode response = apiCall.apply(client);

        TourApiResponse apiResponse = new TourApiResponse(response);
        apiResponse.validError();

        List<Map<String, String>> items = apiResponse.getItems();

        if (items.isEmpty()) {
            return List.of();
        }

        postProcessor.accept(items);

        return items;
    }

    public List<Map<String, String>> fetchItems(Function<TourApiClient, JsonNode> apiCall) {
        return fetchItems(apiCall, items -> {});
    }
}
