package project.airbnb.clone.service.tour;

import com.fasterxml.jackson.databind.JsonNode;
import project.airbnb.clone.dto.OpenApiResponse;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public record HttpClientTemplate<T>(T client) {

    public List<Map<String, String>> fetchItems(Function<T, JsonNode> apiCall,
                                                Consumer<List<Map<String, String>>> postProcessor) {
        JsonNode response = apiCall.apply(client);

        OpenApiResponse apiResponse = new OpenApiResponse(response);
        apiResponse.validError();

        List<Map<String, String>> items = apiResponse.getItems();

        if (items.isEmpty()) {
            return List.of();
        }

        postProcessor.accept(items);

        return items;
    }

    public List<Map<String, String>> fetchItems(Function<T, JsonNode> apiCall) {
        return fetchItems(apiCall, items -> { });
    }
}
