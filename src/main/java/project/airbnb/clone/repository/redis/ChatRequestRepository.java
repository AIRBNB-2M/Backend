package project.airbnb.clone.repository.redis;

import org.springframework.data.repository.CrudRepository;
import project.airbnb.clone.repository.dto.ChatRequest;

import java.util.List;

public interface ChatRequestRepository extends CrudRepository<ChatRequest, String> {
    List<ChatRequest> findBySenderId(Long senderId);

    List<ChatRequest> findByReceiverId(Long receiverId);
}
