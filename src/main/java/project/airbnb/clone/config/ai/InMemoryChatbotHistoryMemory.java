package project.airbnb.clone.config.ai;

import org.springframework.ai.chat.messages.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryChatbotHistoryMemory implements ChatbotHistoryMemory {

    Map<String, List<ChatbotHistoryDto>> chatbotHistoryStore = new ConcurrentHashMap<>();

    @Override
    public void save(String conversationId, Message message) {
        chatbotHistoryStore.putIfAbsent(conversationId, new ArrayList<>());
        chatbotHistoryStore.get(conversationId).add(ChatbotHistoryDto.of(message));
    }

    @Override
    public List<ChatbotHistoryDto> getMessages(String conversationId) {
        return new ArrayList<>(chatbotHistoryStore.getOrDefault(conversationId, List.of()));
    }
}
