package project.airbnb.clone.config.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import project.airbnb.clone.repository.mongo.ChatbotHistoryMongoRepository;

import java.util.List;

@Primary
@Component
@RequiredArgsConstructor
public class MongoChatbotHistoryMemory implements ChatbotHistoryMemory {

    private final ChatbotHistoryMongoRepository chatbotHistoryMongoRepository;

    @Override
    public void save(String conversationId, Message message) {
        ChatbotHistoryDocument document = ChatbotHistoryDocument.of(conversationId, message);
        chatbotHistoryMongoRepository.save(document);
    }

    @Override
    public List<ChatbotHistoryDto> getMessages(String conversationId) {
        return chatbotHistoryMongoRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
                                            .stream()
                                            .map(ChatbotHistoryDto::of)
                                            .toList();
    }
}
