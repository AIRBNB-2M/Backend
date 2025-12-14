package project.airbnb.clone.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import project.airbnb.clone.config.ai.ChatbotHistoryDocument;

import java.util.List;

public interface ChatbotHistoryMongoRepository extends MongoRepository<ChatbotHistoryDocument, String> {
    List<ChatbotHistoryDocument> findByConversationIdOrderByCreatedAtAsc(String conversationId);
}
