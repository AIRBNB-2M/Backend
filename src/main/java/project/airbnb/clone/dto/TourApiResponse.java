package project.airbnb.clone.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class TourApiResponse {

    private final List<Map<String, String>> items;
    private int totalCount;

    public TourApiResponse(JsonNode response) {
        this.items = new ArrayList<>();

        JsonNode body = response.path("response").path("body");
        if (body.get("totalCount") != null) {
            this.totalCount = Integer.parseInt(body.get("totalCount").asText());
        }

        JsonNode items = body.path("items").path("item");

        for (JsonNode item : items) {
            Map<String, String> itemMap = new HashMap<>();

            for (Map.Entry<String, JsonNode> entry : item.properties()) {
                String key = entry.getKey();
                String value = entry.getValue().asText();

                itemMap.put(key, value);
            }

            this.items.add(itemMap);
        }
    }
}
