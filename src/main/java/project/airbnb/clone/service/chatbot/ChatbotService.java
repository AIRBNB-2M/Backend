package project.airbnb.clone.service.chatbot;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import project.airbnb.clone.config.ai.ChatbotHistoryDto;
import project.airbnb.clone.config.ai.CustomMessageChatMemoryAdvisor;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatClient chatClient;
    private final CustomMessageChatMemoryAdvisor loginMemoryAdvisor;
    private final CustomMessageChatMemoryAdvisor anonymousMemoryAdvisor;

    public Flux<String> postMessage(Long memberId, String message, HttpSession session) {
        boolean isLogin = memberId != null;

        String conversationId = isLogin ? memberId.toString() : getConversationId(session);

        CustomMessageChatMemoryAdvisor advisor = isLogin ? loginMemoryAdvisor : anonymousMemoryAdvisor;

        return chatClient.prompt()
                         .user(message)
                         .advisors(adv -> adv
                                 .advisors(advisor)
                                 .param(ChatMemory.CONVERSATION_ID, conversationId)
                         )
                         .stream()
                         .content();
    }

    public List<ChatbotHistoryDto> getMessages(Long memberId, HttpSession session) {
        boolean isLogin = memberId != null;

        String conversationId = isLogin ? memberId.toString() : getConversationId(session);

        CustomMessageChatMemoryAdvisor advisor = isLogin ? loginMemoryAdvisor : anonymousMemoryAdvisor;
        return advisor.getMessages(conversationId);
    }

    private String getConversationId(HttpSession session) {
        String sessionKey = "conversationId";
        String conversationId = (String) session.getAttribute(sessionKey);

        if (!StringUtils.hasText(conversationId)) {
            conversationId = UUID.randomUUID().toString();
            session.setAttribute(sessionKey, conversationId);
        }

        return conversationId;
    }
}
