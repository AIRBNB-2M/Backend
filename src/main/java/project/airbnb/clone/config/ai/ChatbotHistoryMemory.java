package project.airbnb.clone.config.ai;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

public interface ChatbotHistoryMemory {
    void save(String conversationId, Message message);
    List<ChatbotHistoryDto> getMessages(String conversationId);
}
